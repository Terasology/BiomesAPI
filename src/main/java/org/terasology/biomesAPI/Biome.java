// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.biomesAPI;

import org.terasology.gestalt.naming.Name;

/**
 * Biomes can be assigned to different blocks during worldgen as well as on runtime, to provide additional metadata
 * about player's surroundings usable to enhance player experience.
 * <p>
 * Biomes are easiest implemented in enums, and are meant to be implemented like that.
 *
 * @see OnBiomeChangedEvent
 */
public interface Biome {

    /**
     * @return An identifier that includes both the Module the biome originates from and a unique biome id (unique to
     *         that module).
     */
    Name getId();

    /**
     * Returns human readable name of the biome.
     */
    String getDisplayName();

    /**
     * Biome hashCode must be deterministic, non-zero, and unique for every biome.
     * <p>
     * Please consider overriding this method to return constant values, hard-coded for each of the biomes. No
     * assumptions should however be made from any external module using biomes about their constant value, i.e. modules
     * should always retrieve biome hash using this function, and not hard-code any constant values.
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
