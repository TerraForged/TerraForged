package com.terraforged.biome.modifier;

import com.terraforged.api.biome.modifier.BiomeModifier;
import com.terraforged.core.cell.Cell;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class MushroomModifier implements BiomeModifier {
    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean test(Biome biome) {
        return biome == Biomes.MUSHROOM_FIELD_SHORE;
    }

    @Override
    public Biome modify(Biome in, Cell cell, int x, int z) {
        return Biomes.MUSHROOM_FIELDS;
    }
}
