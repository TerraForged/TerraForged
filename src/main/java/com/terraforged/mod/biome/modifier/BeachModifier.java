package com.terraforged.mod.biome.modifier;

import com.terraforged.api.biome.modifier.BiomeModifier;
import com.terraforged.core.cell.Cell;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.terrain.TerrainType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class BeachModifier implements BiomeModifier {

    private final float height;
    private final Module noise;
    private final BiomeMap biomes;
    private final Biome mushroomFields;
    private final Biome mushroomFieldShore;

    public BeachModifier(BiomeMap biomeMap, TerraContext context) {
        this.biomes = biomeMap;
        this.height = context.levels.water(6);
        this.noise = Source.perlin(context.seed.next(), 15, 1).scale(context.levels.scale(5));
        this.mushroomFields = context.gameContext.biomes.get(Biomes.MUSHROOM_FIELDS);
        this.mushroomFieldShore = context.gameContext.biomes.get(Biomes.MUSHROOM_FIELD_SHORE);
    }

    @Override
    public int priority() {
        return 9;
    }

    @Override
    public boolean test(Biome biome, Cell cell) {
        return cell.terrain.getType() == TerrainType.BEACH && cell.biomeType != BiomeType.DESERT;
    }

    @Override
    public Biome modify(Biome in, Cell cell, int x, int z) {
        if (cell.value + noise.getValue(x, z) < height) {
            if (in == mushroomFields) {
                return mushroomFieldShore;
            }
            return biomes.getBeach(cell);
        }
        return in;
    }
}
