// Copyright 2018 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.biomesAPI;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event is thrown to entities with {@link org.terasology.engine.logic.players.PlayerCharacterComponent} whenever they
 * change the biome they are in.
 */
public class OnBiomeChangedEvent implements Event {
    private Biome oldBiome;
    private Biome newBiome;

    public OnBiomeChangedEvent(Biome oldBiome, Biome newBiome) {
        this.oldBiome = oldBiome;
        this.newBiome = newBiome;
    }

    /**
     * @return Biome the entity just left
     */
    public Biome getOldBiome() {
        return oldBiome;
    }

    /**
     * @return Biome the entity just entered
     */
    public Biome getNewBiome() {
        return newBiome;
    }
}
