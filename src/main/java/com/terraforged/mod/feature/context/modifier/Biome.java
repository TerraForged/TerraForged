package com.terraforged.mod.feature.context.modifier;

import com.mojang.serialization.Dynamic;
import com.terraforged.mod.feature.context.ChanceContext;
import net.minecraft.util.math.BlockPos;

public class Biome extends RangeContextModifier {

    public Biome(float from, float to, boolean exclusive) {
        super(from, to, exclusive);
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
        return RangeContextModifier.deserialize(dynamic, Biome::new);
    }
}
