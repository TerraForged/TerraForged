package com.terraforged.mod.feature.context.modifier;

import com.mojang.datafixers.Dynamic;
import com.terraforged.mod.feature.context.ChanceContext;
import net.minecraft.util.math.BlockPos;

public class Biome extends RangeContextModifier {

    public Biome(float chance, float min, float max) {
        super(min, max);
    }

    @Override
    public String getName() {
        return "biome";
    }

    @Override
    protected float getValue(BlockPos pos, ChanceContext context) {
        return context.cell.biomeEdge;
    }

    public static ContextModifier deserialize(Dynamic<?> dynamic) {
        float chance = dynamic.get("chance").asFloat(0F);
        float min = dynamic.get("min").asFloat(0F);
        float max = dynamic.get("max").asFloat(1F);
        return new Biome(chance, min, max);
    }
}
