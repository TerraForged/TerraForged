package com.terraforged.mod.biome.map.defaults;

import com.terraforged.core.cell.Cell;
import net.minecraft.world.biome.Biome;

public interface DefaultBiome {

    Biome getBiome(float temperature);

    default Biome getDefaultBiome(Cell cell) {
        return getBiome(cell.temperature).delegate.get();
    }

    default Biome getDefaultBiome(float temperature) {
        return getBiome(temperature).delegate.get();
    }
}
