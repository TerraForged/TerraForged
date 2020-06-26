package com.terraforged.mod.feature.context;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OptionalDynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.terraforged.mod.feature.context.modifier.ContextModifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContextualFeature {

    public final float chance;
    public final List<ContextModifier> contexts;
    public final ConfiguredFeature<?, ?> feature;

    public ContextualFeature(ConfiguredFeature<?, ?> feature, float chance, List<ContextModifier> contexts) {
        this.chance = chance;
        this.feature = feature;
        this.contexts = contexts;
    }

    public String getName() {
        return "chance";
    }

    public float getChance(BlockPos pos, ChanceContext context) {
        float chance = this.chance;
        for (ContextModifier modifier : contexts) {
            chance *= modifier.getChance(pos, context);
        }
        return chance;
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("feature"), feature.serialize(ops).getValue(),
                ops.createString("context"), serializeContext(ops).getValue()
        )));
    }

    private <T> Dynamic<T> serializeContext(DynamicOps<T> ops) {
        Map<T, T> map = new LinkedHashMap<>();
        map.put(ops.createString("chance"), ops.createFloat(chance));
        for (ContextModifier context : contexts) {
            map.put(ops.createString(context.getName()), context.serialize(ops).getValue());
        }
        return new Dynamic<>(ops, ops.createMap(map));
    }

    public static ContextualFeature deserialize(Dynamic<?> dynamic) {
        ConfiguredFeature<?, ?> feature = deserializeFeature(dynamic.get("feature"));
        OptionalDynamic<?> context = dynamic.get("context");
        float chance = context.get("chance").asFloat(0);
        List<ContextModifier> contexts = deserializeContexts(context);
        return new ContextualFeature(feature, chance, contexts);
    }

    private static ConfiguredFeature<?, ?> deserializeFeature(OptionalDynamic<?> dynamic) {
        return dynamic.map(ConfiguredFeature::deserialize).orElseThrow(NullPointerException::new);
    }

    private static List<ContextModifier> deserializeContexts(OptionalDynamic<?> dynamic) {
        return dynamic.map(d -> d.getMapValues().map(map -> {
            List<ContextModifier> contexts = new ArrayList<>(map.size());
            map.forEach((key, value) -> {
                String name = key.asString("");
                if (!name.equals("chance")) {
                    contexts.add(ContextModifier.parse(name, value));
                }
            });
            return contexts;
        }).orElse(Collections.emptyList())).orElseThrow(NullPointerException::new);
    }
}
