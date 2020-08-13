package com.terraforged.mod.feature.context.modifier;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.feature.context.ChanceContext;
import net.minecraft.util.math.BlockPos;

public class None implements ContextModifier {

    @Override
    public String getName() {
        return "none";
    }

    @Override
    public float getChance(BlockPos pos, ChanceContext context) {
        return 0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops);
    }
}
