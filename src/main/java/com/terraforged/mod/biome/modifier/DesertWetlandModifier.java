package com.terraforged.mod.biome.modifier;

import com.terraforged.api.biome.modifier.BiomeModifier;
import com.terraforged.core.cell.Cell;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.terrain.TerrainType;
import net.minecraft.world.biome.Biome;

public class DesertWetlandModifier implements BiomeModifier {

    private final BiomeMap biomes;

    public DesertWetlandModifier(BiomeMap biomes) {
        this.biomes = biomes;
    }

    @Override
    public int priority() {
        // higher than desert colour mod
        return 6;
    }

    @Override
    public boolean exitEarly() {
        return true;
    }

    @Override
    public boolean test(Biome biome, Cell cell) {
        return cell.terrain.getType() == TerrainType.WETLAND && cell.biomeType == BiomeType.DESERT;
    }

    @Override
    public Biome modify(Biome in, Cell cell, int x, int z) {
        return biomes.getLandSet().getBiome(getBiomeType(cell), cell.temperature, cell.biomeIdentity);
    }

    private static BiomeType getBiomeType(Cell cell) {
        return cell.biomeIdentity < 0.5F ? BiomeType.SAVANNA : BiomeType.STEPPE;
    }
}
