package com.terraforged.mod.biome.map.set;

import com.terraforged.core.cell.Cell;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.map.defaults.DefaultBiomes;
import com.terraforged.mod.biome.utils.TempCategory;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Map;

public class WetlandSet extends TemperatureSet {

    private final BiomeMap fallback;

    public WetlandSet(Map<TempCategory, List<Biome>> map, BiomeMap fallback, GameContext context) {
        super(map, DefaultBiomes.WETLAND, context);
        this.fallback = fallback;
    }

    @Override
    public Biome getBiome(Cell cell) {
        Biome biome = super.getBiome(cell);
        if (biome == null) {
            return fallback.getLand(cell);
        }
        return biome;
    }
}
