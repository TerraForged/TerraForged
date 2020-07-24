package com.terraforged.mod.biome.map.set;

import com.terraforged.core.cell.Cell;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.map.defaults.DefaultBiomes;
import com.terraforged.mod.biome.map.defaults.DefaultWetlands;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Map;

public class WetlandSet extends TemperatureSet {

    private final BiomeMap fallback;

    public WetlandSet(Map<Biome.TempCategory, List<Biome>> map, BiomeMap fallback) {
        super(map, new DefaultWetlands());
        this.fallback = fallback;
    }

    @Override
    public Biome getBiome(Cell cell) {
        Biome biome = super.getBiome(cell);
        if (biome != DefaultBiomes.NONE) {
            return biome;
        }
        return fallback.getLand(cell);
    }
}
