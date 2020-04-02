package com.terraforged.mod.feature.decorator.poisson;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.gen.placement.IPlacementConfig;

public class PoissonConfig implements IPlacementConfig {

    public final int radius;
    public final int variance;

    public PoissonConfig(int radius, int variance) {
        this.radius = radius;
        this.variance = variance;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(
                ImmutableMap.of(
                        ops.createString("radius"), ops.createInt(radius),
                        ops.createString("variance_scale"), ops.createInt(variance)
                ))
        );
    }

    public static PoissonConfig deserialize(Dynamic<?> dynamic) {
        int radius = dynamic.get("radius").asInt(4);
        int variance = dynamic.get("variance_scale").asInt(0);
        return new PoissonConfig(radius, variance);
    }
}
