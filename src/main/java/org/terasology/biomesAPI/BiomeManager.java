// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.biomesAPI;

import com.google.common.base.Preconditions;
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
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.metrics.DebugMetricsSystem;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.blockdata.ExtraDataSystem;
import org.terasology.engine.world.chunks.blockdata.RegisterExtraData;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Share(BiomeRegistry.class)
@RegisterSystem
@ExtraDataSystem
public class BiomeManager extends BaseComponentSystem implements BiomeRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeManager.class);
    private final Map<Short, Biome> biomeMap = new HashMap<>();
    @In
    DebugMetricsSystem debugMetricsSystem;
    @In
    private EntityManager entityManager;
    @In
    private NUIManager nuiManager;
    @In
    private WorldProvider worldProvider;
    private BiomesMetricsMode metricsMode;

    private int biomeHashIndex;

    /**
     * Blocks have id, no matter what kind of blocks they are.
     */
    @RegisterExtraData(name = "BiomesAPI.biomeHash", bitSize = 16)
    public static boolean hasBiome(Block block) {
        return true;
    }

    @Override
    public Optional<Biome> getBiome(Vector3i pos) {
        return getBiome(pos.x, pos.y, pos.z);
    }

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
    public void setBiome(Biome biome, CoreChunk chunk, int relX, int relY, int relZ) {
        Preconditions.checkArgument(biomeMap.containsKey(biome.biomeHash()), "Trying to use non-registered biome!");
        biomeHashIndex = CoreRegistry.get(ExtraBlockDataManager.class).getSlotNumber("BiomesAPI.biomeHash");
        chunk.setExtraData(biomeHashIndex, new Vector3i(relX, relY, relZ), biome.biomeHash());
    }

    @Override
    public void setBiome(Biome biome, Vector3i pos) {
        setBiome(biome, pos.x, pos.y, pos.z);
    }

    @Override
    public void setBiome(Biome biome, CoreChunk chunk, Vector3i pos) {
        setBiome(biome, chunk, pos.x, pos.y, pos.z);
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

    @Override
    public void registerBiome(Biome biome) {
        Preconditions.checkArgument(!biomeMap.containsKey(biome.biomeHash()), "Registering biome with same hash as " +
                "one of previously registered biomes!");
        biomeMap.put(biome.biomeHash(), biome);
        LOGGER.info("Registered biome " + biome.getId() + " with id " + biome.biomeHash());
    }

    /**
     * Responsible for sending {@link OnBiomeChangedEvent} to the player entity.
     */
    @ReceiveEvent(components = PlayerCharacterComponent.class)
    public void checkBiomeChangeEvent(MovedEvent event, EntityRef entity) {
        final Vector3i newPosition = new Vector3i(event.getPosition());
        final Vector3i oldPosition = new Vector3i(new Vector3f(event.getPosition()).sub(event.getDelta()));
        if (!newPosition.equals(oldPosition)) {
            final Optional<Biome> newBiomeOptional = getBiome(newPosition);
            final Optional<Biome> oldBiomeOptional = getBiome(oldPosition);
            if (oldBiomeOptional.isPresent() != newBiomeOptional.isPresent()) {
                throw new RuntimeException("Either all blocks in world must have biome, or none.");
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
    public void checkPlayerSpawnedEvent(OnPlayerSpawnedEvent event, EntityRef entity,
                                        LocationComponent locationComponent) {
        Vector3i spawnPos = new Vector3i(locationComponent.getWorldPosition());
        getBiome(spawnPos)
                .ifPresent(spawnBiome -> metricsMode.setBiome(spawnBiome.getId().toString()));

    }
}
