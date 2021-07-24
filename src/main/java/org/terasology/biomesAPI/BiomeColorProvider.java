// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.biomesAPI;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.ColorProvider;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

@Share(ColorProvider.class)
@RegisterSystem
public class BiomeColorProvider extends BaseComponentSystem implements ColorProvider {
    @In
    private BiomeRegistry biomeRegistry;

    /* LUTs */
    private Texture colorLut;

    private Texture foliageLut;

    public BiomeColorProvider() {
        colorLut = Assets.getTexture("engine:grasscolor").get();
        foliageLut = Assets.getTexture("engine:foliagecolor").get();
    }

    @Override
    public Colorc colorLut(int x, int y, int z) {
        return biomeRegistry.getBiome(x, y, z).map(biome -> {
            float humidity = biome.getHumidity();
            float temperature = biome.getTemperature();
            float prod = humidity * temperature;
            return colorLut.getData().getPixel(
                    (int) ((1 - temperature) * 255),
                    (int) ((1 - prod) * 255)
            );
        }).orElse(Color.white);
    }

    @Override
    public Colorc foliageLut(int x, int y, int z) {
        return biomeRegistry.getBiome(x, y, z).map(biome -> {
            float humidity = biome.getHumidity();
            float temperature = biome.getTemperature();
            float prod = humidity * temperature;
            return foliageLut.getData().getPixel(
                    (int) ((1 - temperature) * 255),
                    (int) ((1 - prod) * 255)
            );
        }).orElse(Color.white);
    }
}
