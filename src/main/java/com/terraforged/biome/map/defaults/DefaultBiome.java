package com.terraforged.biome.map.defaults;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.OceanBiome;

public interface DefaultBiome {

    Biome NONE = new OceanBiome().setRegistryName("terraforged", "none");

    Biome getBiome(float temperature);

    default Biome getDefaultBiome(float temperature) {
        return getBiome(temperature).delegate.get();
    }
}
