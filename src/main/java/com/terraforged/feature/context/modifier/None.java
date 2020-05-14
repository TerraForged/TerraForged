package com.terraforged.feature.context.modifier;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.terraforged.feature.context.ChanceContext;
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
