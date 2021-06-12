/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.biomesAPI;

import com.google.common.base.Preconditions;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.physics.events.MovedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.metrics.DebugMetricsSystem;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.blockdata.ExtraDataSystem;
import org.terasology.engine.world.chunks.blockdata.RegisterExtraData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Share(BiomeRegistry.class)
@RegisterSystem
@ExtraDataSystem
public class BiomeManager extends BaseComponentSystem implements BiomeRegistry {

    private static final Logger logger = LoggerFactory.getLogger(BiomeManager.class);

    @In
    protected EntityManager entityManager;
    @In
    protected NUIManager nuiManager;
    @In
    protected WorldProvider worldProvider;
    @In
    protected DebugMetricsSystem debugMetricsSystem;
    @In
    protected ExtraBlockDataManager blockDataManager;

    private final Map<Short, Biome> biomeMap = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeManager.class);

    private BiomesMetricsMode metricsMode;

    private int biomeHashIndex;

    @Override
    public Optional<Biome> getBiome(int x, int y, int z) {
        final short biomeHash = (short) worldProvider.getExtraData("BiomesAPI.biomeHash", x, y, z);
        if (biomeHash == 0) {
            return Optional.empty();
        }
        Preconditions.checkArgument(biomeMap.containsKey(biomeHash), "Trying to use non-registered biome!");
        return Optional.of(biomeMap.get(biomeHash));
    }

    @Override
    public void setBiome(Biome biome, int x, int y, int z) {
        Preconditions.checkArgument(biomeMap.containsKey(biome.biomeHash()), "Trying to use non-registered biome!");
        worldProvider.setExtraData("BiomesAPI.biomeHash", x, y, z, biome.biomeHash());
    }

    @Override
    public void setBiome(Biome biome, Chunk chunk, int relX, int relY, int relZ) {
        Preconditions.checkArgument(biomeMap.containsKey(biome.biomeHash()), "Trying to use non-registered biome!");
        biomeHashIndex = blockDataManager.getSlotNumber("BiomesAPI.biomeHash");
        chunk.setExtraData(biomeHashIndex, new org.joml.Vector3i(relX, relY, relZ), biome.biomeHash());
    }

    @Override
    public <T extends Biome> List<T> getRegisteredBiomes(Class<T> biomeClass) {
        return biomeMap.values().stream().filter(biomeClass::isInstance).map(biomeClass::cast).collect(Collectors.toList());
    }

    @Override
    public void preBegin() {
        metricsMode = new BiomesMetricsMode();
        debugMetricsSystem.register(metricsMode);
    }

    /**
     * Blocks have id, no matter what kind of blocks they are.
     */
    @RegisterExtraData(name = "BiomesAPI.biomeHash", bitSize = 16)
    public static boolean hasBiome(Block block) {
        return true;
    }

    @Override
    public void registerBiome(Biome biome) {
        Preconditions.checkArgument(!biomeMap.containsKey(biome.biomeHash()), "Registering biome with same hash as one of previously registered biomes!");
        biomeMap.put(biome.biomeHash(), biome);
        LOGGER.info("Registered biome " + biome.getId() + " with id " + biome.biomeHash());
    }

    /**
     * Responsible for sending {@link OnBiomeChangedEvent} to the player entity.
     */
    @ReceiveEvent(components = PlayerCharacterComponent.class)
    public void checkBiomeChangeEvent(MovedEvent event, EntityRef entity) {
        final Vector3i newPosition = new Vector3i(event.getPosition(), RoundingMode.FLOOR);
        final Vector3i oldPosition = new Vector3i(event.getPosition().sub(event.getDelta(), new Vector3f()), RoundingMode.FLOOR);
        if (!newPosition.equals(oldPosition)) {
            final Optional<Biome> newBiomeOptional = getBiome(newPosition);
            final Optional<Biome> oldBiomeOptional = getBiome(oldPosition);

            if (oldBiomeOptional.isPresent() != newBiomeOptional.isPresent()) {
                // This usually happens when a player enters a chunk that is not fully loaded yet. This is usually not a
                // problem and the biome changed check will work fine again with the next movement once the chunk is
                // fully loaded.
                // Leaving a WARN message here to see whether this can occur under other circumstances. Eventually, we
                // may reduce the logging level further, probably to DEBUG.
                logger.warn("Missing biome information for {}", !oldBiomeOptional.isPresent() ? oldPosition : newPosition);
                return;
            }
            if (!oldBiomeOptional.isPresent()) {
                return;
            }

            Biome newBiome = newBiomeOptional.get();
            Biome oldBiome = oldBiomeOptional.get();
            if (oldBiome != newBiome) {
                entity.send(new OnBiomeChangedEvent(oldBiome, newBiome));
                metricsMode.setBiome(newBiome.getId().toString());
            }
        }
    }

    @ReceiveEvent(components = PlayerCharacterComponent.class)
    public void checkPlayerSpawnedEvent(OnPlayerSpawnedEvent event, EntityRef entity, LocationComponent locationComponent) {
        Vector3i spawnPos = new Vector3i(locationComponent.getWorldPosition(new Vector3f()), RoundingMode.FLOOR);
        getBiome(spawnPos).ifPresent(spawnBiome -> metricsMode.setBiome(spawnBiome.getId().toString()));
    }
}
