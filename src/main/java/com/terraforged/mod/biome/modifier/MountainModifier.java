package com.terraforged.mod.biome.modifier;

import com.terraforged.api.biome.modifier.BiomeModifier;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.map.defaults.DefaultBiome;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.core.cell.Cell;
import net.minecraft.world.biome.Biome;

public class MountainModifier implements BiomeModifier {

    // the probability that mountain terrain will get upgraded to a mountain biome
    public static final float MOUNTAIN_CHANCE = 0.4F;

    private final float chance;
    private final BiomeMap biomes;

    public MountainModifier(TerraContext context, BiomeMap biomes) {
        this.biomes = biomes;
        this.chance = context.terraSettings.miscellaneous.mountainBiomeUsage;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean exitEarly() {
        return true;
    }

    @Override
    public boolean test(Biome biome, Cell cell) {
        return cell.terrain.isMountain() && cell.macroNoise < chance;
    }

    @Override
    public Biome modify(Biome in, Cell cell, int x, int z) {
        Biome mountain = biomes.getMountain(cell);
        if (mountain != DefaultBiome.NONE) {
            return mountain;
        }
        return in;
    }
}
