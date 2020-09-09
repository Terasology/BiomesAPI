// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.biomesAPI;

import org.terasology.engine.rendering.nui.layers.ingame.metrics.MetricsMode;

/**
 * Display the name of the biome the player is currently located in in the debug overlay.
 * <p>
 * Current biome is set when OnBiomeChanged event is triggered biomeName is polled whenever MetricsMode values are
 * updated
 */
class BiomesMetricsMode extends MetricsMode {

    private String biomeName;

    BiomesMetricsMode() {
        super("\n- Biome Info -");
    }

    @Override
    public String getMetrics() {
        return getName() + "\n" + biomeName;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isPerformanceManagerMode() {
        return false;
    }

    public void setBiome(String biomeName) {
        this.biomeName = biomeName;
    }
}
