package com.terraforged.mod.feature.decorator.poisson;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.gen.placement.IPlacementConfig;

public class PoissonConfig implements IPlacementConfig {

    public final int radius;
    public final float biomeFade;
    public final int variance;
    public final float min;
    public final float max;

    public PoissonConfig(int radius, float biomeFade, int variance, float min, float max) {
        this.radius = radius;
        this.biomeFade = biomeFade;
        this.variance = variance;
        this.min = min;
        this.max = max;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(
                ImmutableMap.of(
                        ops.createString("radius"), ops.createInt(radius),
                        ops.createString("fade"), ops.createFloat(biomeFade),
                        ops.createString("scale"), ops.createInt(variance),
                        ops.createString("min"), ops.createFloat(min),
                        ops.createString("max"), ops.createFloat(max)
                ))
        );
    }

    public static PoissonConfig deserialize(Dynamic<?> dynamic) {
        int radius = dynamic.get("radius").asInt(4);
        float biomeFade = dynamic.get("fade").asFloat(0.2F);
        int variance = dynamic.get("scale").asInt(0);
        float min = dynamic.get("min").asFloat(0F);
        float max = dynamic.get("max").asFloat(1F);
        return new PoissonConfig(radius, biomeFade, variance, min, max);
    }
}
