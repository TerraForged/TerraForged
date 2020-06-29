package com.terraforged.mod.biome.map.defaults;

import net.minecraft.world.biome.Biome;

public interface DefaultBiome {

    Biome getBiome(float temperature);

    default Biome getDefaultBiome(float temperature) {
        return getBiome(temperature).delegate.get();
    }
}
