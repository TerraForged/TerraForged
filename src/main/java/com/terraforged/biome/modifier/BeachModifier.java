package com.terraforged.biome.modifier;

import com.terraforged.api.biome.modifier.BiomeModifier;
import com.terraforged.biome.map.BiomeMap;
import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.terrain.Terrains;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class BeachModifier implements BiomeModifier {

    private final float height;
    private final Module noise;
    private final BiomeMap biomes;
    private final Terrains terrains;

    public BeachModifier(BiomeMap biomeMap, GeneratorContext context) {
        this.biomes = biomeMap;
        this.terrains = context.terrain;
        this.height = context.levels.water(6);
        this.noise = Source.perlin(context.seed.next(), 10, 1).scale(context.levels.scale(5));
    }

    @Override
    public int priority() {
        return 9;
    }

    @Override
    public boolean test(Biome biome) {
        return true;
    }

    @Override
    public Biome modify(Biome in, Cell cell, int x, int z) {
        if (cell.terrain == terrains.beach && cell.biomeType != BiomeType.DESERT) {
            if (cell.value + noise.getValue(x, z) < height) {
                if (in == Biomes.MUSHROOM_FIELDS) {
                    return Biomes.MUSHROOM_FIELD_SHORE;
                }
                return biomes.getBeach(cell);
            }
        }
        return in;
    }
}
