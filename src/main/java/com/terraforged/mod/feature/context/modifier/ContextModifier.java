package com.terraforged.mod.feature.context.modifier;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.terraforged.mod.feature.context.ChanceContext;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.function.Function;

public interface ContextModifier {

    Map<String, Function<Dynamic<?>, ContextModifier>> REGISTRY = ImmutableMap.of(
            "elevation", Elevation::deserialize,
            "biome", Biome::deserialize
    );

    ContextModifier NONE = new None();

    String getName();

    float getChance(BlockPos pos, ChanceContext context);

    <T> Dynamic<T> serialize(DynamicOps<T> ops);

    static ContextModifier parse(String type, Dynamic<?> dynamic) {
        Function<Dynamic<?>, ContextModifier> func = REGISTRY.get(type);
        if (func == null) {
            return NONE;
        }
        return func.apply(dynamic);
    }
}
