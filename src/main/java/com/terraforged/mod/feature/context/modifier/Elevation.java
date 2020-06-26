package com.terraforged.mod.feature.context.modifier;

import com.mojang.datafixers.Dynamic;
import com.terraforged.mod.feature.context.ChanceContext;
import net.minecraft.util.math.BlockPos;

public class Elevation extends RangeContextModifier {

    public Elevation(float from, float to, boolean exclusive) {
        super(from, to, exclusive);
    }

    @Override
    public String getName() {
        return "elevation";
    }

    @Override
    protected float getValue(BlockPos pos, ChanceContext context) {
        return context.levels.elevation(context.cell.value);
    }

    public static ContextModifier deserialize(Dynamic<?> dynamic) {
        return RangeContextModifier.deserialize(dynamic, Elevation::new);
    }
}
