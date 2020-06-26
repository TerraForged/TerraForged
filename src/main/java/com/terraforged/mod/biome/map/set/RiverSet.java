package com.terraforged.mod.biome.map.set;

import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.map.defaults.DefaultBiome;
import com.terraforged.mod.biome.map.defaults.DefaultBiomes;
import com.terraforged.core.cell.Cell;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Map;

public class RiverSet extends TemperatureSet {

    private final BiomeMap biomes;

    public RiverSet(Map<Biome.TempCategory, List<Biome>> map, DefaultBiome defaultBiome, BiomeMap biomes) {
        super(map, defaultBiome);
        this.biomes = biomes;
    }

    @Override
    public Biome getBiome(Cell cell) {
        Biome biome = biomes.getLand(cell);
        if (DefaultBiomes.overridesRiver(biome)) {
            return biome;
        }
        return super.getBiome(cell);
    }
}
