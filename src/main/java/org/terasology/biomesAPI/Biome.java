// Copyright 2018 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.biomesAPI;

import org.joml.Vector3ic;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gestalt.naming.Name;

/**
 * Biomes can be assigned to different blocks during worldgen as well as on runtime, to provide additional metadata about player's
 * surroundings usable to enhance player experience.
 * <p>
 * Biomes are easiest implemented in enums, and are meant to be implemented like that.
 *
 * @see OnBiomeChangedEvent
 */
public interface Biome {

    /**
     * @return An identifier that includes both the Module the biome originates from and a unique biome id (unique to that module).
     */
    Name getId();

    /**
     * Returns human readable name of the biome.
     */
    String getDisplayName();

    /**
     * Runs any necessary initialization, like caching block types, when the biome is registered.
     */
    default void initialize() {
    }

    /**
     * @return An average humidity value for the biome, used for grass and foliage colors.
     */
    default float getHumidity() {
        return 0.5f;
    }

    /**
     * @return An average temperature value for the biome, used for grass and foliage colors.
     */
    default float getTemperature() {
        return 0.5f;
    }

    /**
     * @return The block that should be generated as the top layer of the biome at the given position. Defaults to grass.
     */
    default Block getSurfaceBlock(Vector3ic pos, int seaLevel) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        return blockManager.getBlock("CoreAssets:Grass");
    }

    /**
     * @return The block that should be generated at the given position in the biome, which isn't the surface. Defaults to dirt for the
     *         first 32 blocks and then stone below.
     */
    default Block getBelowSurfaceBlock(Vector3ic pos, float density) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        if (density > 32) {
            return blockManager.getBlock("CoreAssets:stone");
        } else {
            return blockManager.getBlock("CoreAssets:Dirt");

        }
    }

    /**
     * Biome hashCode must be deterministic, non-zero, and unique for every biome.
     * <p>
     * Please consider overriding this method to return constant values, hard-coded for each of the biomes. No assumptions should however be
     * made from any external module using biomes about their constant value, i.e. modules should always retrieve biome hash using this
     * function, and not hard-code any constant values.
     *
     * @return Hashcode of the biome
     */
    default short biomeHash() {
        short hash = 0;
        char[] chars = getId().toLowerCase().toCharArray();

        for (char c : chars) {
            hash = (short) (c + 31 * hash);
        }

        return hash;
    }
}
