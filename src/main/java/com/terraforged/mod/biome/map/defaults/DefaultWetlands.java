package com.terraforged.mod.biome.map.defaults;

import com.terraforged.core.cell.Cell;
import com.terraforged.mod.biome.ModBiomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class DefaultWetlands implements DefaultBiome {
    @Override
    public Biome getBiome(float temperature) {
        return DefaultBiomes.NONE;
    }

    @Override
    public Biome getDefaultBiome(Cell cell) {
        if (cell.temperature > BiomeTemps.COLD) {
            if (cell.moisture > 0.65F) {
                return Biomes.SWAMP.delegate.get();
            }

            if (cell.moisture > 0.4F) {
                return ModBiomes.MARSHLAND.delegate.get();
            }
        }
        return DefaultBiomes.NONE;
    }
}
