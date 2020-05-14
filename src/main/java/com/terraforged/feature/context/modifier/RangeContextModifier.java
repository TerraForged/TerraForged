package com.terraforged.feature.context.modifier;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.terraforged.feature.context.ChanceContext;
import com.terraforged.util.RangeModifier;
import net.minecraft.util.math.BlockPos;

public abstract class RangeContextModifier extends RangeModifier implements ContextModifier {

    public RangeContextModifier(float min, float max) {
        super(min, max);
    }

    @Override
    public float getChance(BlockPos pos, ChanceContext context) {
        return apply(getValue(pos, context));
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("min"), ops.createFloat(min),
                ops.createString("max"), ops.createFloat(max)
        )));
    }

    protected abstract float getValue(BlockPos pos, ChanceContext context);
}
