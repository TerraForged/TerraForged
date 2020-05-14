package com.terraforged.feature.context;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.List;

public class ContextSelectorConfig implements IFeatureConfig {

    public final List<ContextualFeature> features;

    public ContextSelectorConfig(List<ContextualFeature> features) {
        this.features = features;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("features"), ops.createList(features.stream().map(f -> f.serialize(ops).getValue())
        ))));
    }

    public static <T> ContextSelectorConfig deserialize(Dynamic<T> dynamic) {
        return new ContextSelectorConfig(dynamic.get("features").asList(ContextualFeature::deserialize));
    }
}
