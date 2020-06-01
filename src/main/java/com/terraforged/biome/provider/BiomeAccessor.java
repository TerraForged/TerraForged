package com.terraforged.biome.provider;

import com.terraforged.biome.map.BiomeMap;
import com.terraforged.core.cell.Cell;
import net.minecraft.world.biome.Biome;

public interface BiomeAccessor {

    Biome getBiome(BiomeMap map, Cell cell);
}
