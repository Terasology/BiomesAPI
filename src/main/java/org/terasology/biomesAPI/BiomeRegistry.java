// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.biomesAPI;

import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.engine.world.ChunkView;
import org.terasology.engine.world.chunks.Chunk;

import java.util.Collection;
import java.util.Optional;

public interface BiomeRegistry {
    /**
     * This method must be called on every startup to register biomes with this module.
     * <p>
     * Biomes should be ideally registered through new {@link ComponentSystem}, in the method
     * {@link ComponentSystem#preBegin()}.
     *
     * @param biome Biome to register
     */
    void registerBiome(Biome biome);

    /**
     * Sets specified biome at position in world.
     *
     * @param biome Biome to set
     * @param x     x position of the block to set
     * @param y     y position of the block to set
     * @param z     z position of the block to set
     */
    void setBiome(Biome biome, int x, int y, int z);

    /**
     * Sets specified biome at position in world.
     *
     * @param biome Biome to set
     * @param pos   Position of the block to set
     */
    default void setBiome(Biome biome, Vector3ic pos) {
        setBiome(biome, pos.x(), pos.y(), pos.z());
    }

    /**
     * Sets specified biome at position in chunk.
     *
     * @param biome Biome to set
     * @param chunk Chunk where to set the biome
     * @param relX  x position of the block to set, relative to the chunk
     * @param relY  y position of the block to set, relative to the chunk
     * @param relZ  z position of the block to set, relative to the chunk
     */
    void setBiome(Biome biome, Chunk chunk, int relX, int relY, int relZ);

    /**
     * Sets specified biome at position in chunk.
     *
     * @param biome Biome to set
     * @param chunk Chunk where to set the biome
     * @param pos   Position of the block to set
     */
    default void setBiome(Biome biome, Chunk chunk, Vector3ic pos) {
        setBiome(biome, chunk, pos.x(), pos.y(), pos.z());
    }

    /**
     * Gets biome at position in world.
     *
     * @param pos Position of the block to get biome of.
     * @return Biome of the block
     */
    default Optional<Biome> getBiome(Vector3ic pos) {
        return getBiome(pos.x(), pos.y(), pos.z());
    };

    /**
     * Gets biome at position in world.
     *
     * @param x x position of the block to get biome of
     * @param y y position of the block to get biome of
     * @param z z position of the block to get biome of
     * @return Biome of the block
     */
    Optional<Biome> getBiome(int x, int y, int z);

    /**
     * Gets biome at position in chunk view.
     *
     * @param relX x position of the block to get biome of, relative to the chunk view
     * @param relY y position of the block to get biome of, relative to the chunk view
     * @param relZ z position of the block to get biome of, relative to the chunk view
     * @return Biome of the block
     */
    Optional<Biome> getBiome(ChunkView view, int relX, int relY, int relZ);

    /**
     * Returns all registered biomes of specified subtype.
     *
     * @param biomeClass Type of the biomes to get
     * @return Collection of biomes of given subtype
     */
    <T extends Biome> Collection<T> getRegisteredBiomes(Class<T> biomeClass);
}
