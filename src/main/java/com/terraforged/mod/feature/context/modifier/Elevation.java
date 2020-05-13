package com.terraforged.mod.feature.context.modifier;

import com.mojang.datafixers.Dynamic;
import com.terraforged.mod.feature.context.ChanceContext;
import net.minecraft.util.math.BlockPos;

public class Elevation extends RangeContextModifier {

    public Elevation(float min, float max) {
        super(min, max);
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
        float min = dynamic.get("min").asFloat(0F);
        float max = dynamic.get("max").asFloat(1F);
        return new Elevation(min, max);
    }
}
