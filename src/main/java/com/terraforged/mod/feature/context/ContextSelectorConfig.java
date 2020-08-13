package com.terraforged.mod.feature.context;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.fm.util.codec.Codecs;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.List;

public class ContextSelectorConfig implements IFeatureConfig {

    public static final Codec<ContextSelectorConfig> CODEC = Codecs.create(
            ContextSelectorConfig::serialize,
            ContextSelectorConfig::deserialize
    );

    public final List<ContextualFeature> features;

    public ContextSelectorConfig(List<ContextualFeature> features) {
        this.features = features;
    }

    public static <T> Dynamic<T> serialize(ContextSelectorConfig config, DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("features"), Codecs.createList(ContextualFeature.CODEC, ops, config.features)
        )));
    }

    public static <T> ContextSelectorConfig deserialize(Dynamic<T> dynamic) {
        return new ContextSelectorConfig(dynamic.get("features").asList(ContextualFeature::deserialize));
    }
}
