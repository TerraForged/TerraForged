package com.terraforged.feature.context.modifier;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.terraforged.feature.context.ChanceContext;
import com.terraforged.util.RangeModifier;
import net.minecraft.util.math.BlockPos;

public abstract class RangeContextModifier extends RangeModifier implements ContextModifier {

    public RangeContextModifier(float min, float max, boolean exclusive) {
        super(min, max, exclusive);
    }

    protected abstract float getValue(BlockPos pos, ChanceContext context);

    @Override
    public float getChance(BlockPos pos, ChanceContext context) {
        return apply(getValue(pos, context));
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("from"), ops.createFloat(from),
                ops.createString("to"), ops.createFloat(to),
                ops.createString("exclusive"), ops.createBoolean(max > 0)
        )));
    }

    public static <T extends RangeContextModifier> T deserialize(Dynamic<?> dynamic, Factory<T> factory) {
        float from = dynamic.get("from").asFloat(0F);
        float to = dynamic.get("to").asFloat(1F);
        boolean exclusive = dynamic.get("exclusive").asBoolean(false);
        return factory.create(from, to, exclusive);
    }

    public interface Factory<T extends RangeContextModifier> {

        T create(float from, float to, boolean exclusive);
    }
}
