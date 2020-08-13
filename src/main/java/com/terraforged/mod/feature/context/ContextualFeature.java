package com.terraforged.mod.feature.context;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.terraforged.fm.FeatureSerializer;
import com.terraforged.fm.util.codec.Codecs;
import com.terraforged.fm.util.codec.EncodingException;
import com.terraforged.mod.feature.context.modifier.ContextModifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContextualFeature {

    public static final Codec<ContextualFeature> CODEC = Codecs.create(
            ContextualFeature::serialize,
            ContextualFeature::deserialize
    );

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

    public static <T> Dynamic<T> serialize(ContextualFeature feature, DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("feature"), FeatureSerializer.encode(feature.feature, ops),
                ops.createString("context"), serializeContext(feature, ops).getValue()
        )));
    }

    private static <T> Dynamic<T> serializeContext(ContextualFeature feature, DynamicOps<T> ops) {
        Map<T, T> map = new LinkedHashMap<>();
        map.put(ops.createString("chance"), ops.createFloat(feature.chance));
        for (ContextModifier context : feature.contexts) {
            map.put(ops.createString(context.getName()), context.serialize(ops).getValue());
        }
        return new Dynamic<>(ops, ops.createMap(map));
    }

    public static <T> ContextualFeature deserialize(Dynamic<T> dynamic) {
        ConfiguredFeature<?, ?> feature = FeatureSerializer.decode(dynamic.get("feature")).orElseThrow(EncodingException::new);
        OptionalDynamic<?> context = dynamic.get("context");
        float chance = context.get("chance").asFloat(0);
        List<ContextModifier> contexts = deserializeContexts(context);
        return new ContextualFeature(feature, chance, contexts);
    }

    private static <T> ConfiguredFeature<?, ?> deserializeFeature(OptionalDynamic<T> dynamic) {
        return FeatureSerializer.decode(dynamic).orElseThrow(EncodingException::new);
    }

    private static List<ContextModifier> deserializeContexts(OptionalDynamic<?> dynamic) {
        return dynamic.flatMap(d -> d.getMapValues().map(map -> {
            List<ContextModifier> contexts = new ArrayList<>(map.size());
            map.forEach((key, value) -> {
                String name = key.asString("");
                if (!name.equals("chance")) {
                    contexts.add(ContextModifier.parse(name, value));
                }
            });
            return contexts;
        })).result().orElseThrow(EncodingException::new);
    }
}
