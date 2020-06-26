package com.terraforged.mod.feature.decorator.poisson;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.terraforged.core.util.poisson.PoissonContext;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.placement.IPlacementConfig;

public class PoissonConfig implements IPlacementConfig {

    public final int radius;
    public final float biomeFade;
    public final int densityNoiseScale;
    public final float densityNoiseMin;
    public final float densityNoiseMax;

    public PoissonConfig(int radius, float biomeFade, int densityNoiseScale, float densityNoiseMin, float densityNoiseMax) {
        this.radius = radius;
        this.biomeFade = biomeFade;
        this.densityNoiseScale = densityNoiseScale;
        this.densityNoiseMin = densityNoiseMin;
        this.densityNoiseMax = densityNoiseMax;
    }

    public void apply(IWorld world, ChunkGenerator<?> generator, PoissonContext context) {
        Module fade = Source.ONE;
        Module density = Source.ONE;

        if (biomeFade > 0.075F) {
            fade = BiomeVariance.of(world, generator, biomeFade);
        }

        if (densityNoiseScale > 0) {
            density = Source.simplex(context.seed, densityNoiseScale, 1)
                    .scale(densityNoiseMax - densityNoiseMin)
                    .bias(densityNoiseMin);
        }

        context.density = mult(fade, density);
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(
                ImmutableMap.of(
                        ops.createString("radius"), ops.createInt(radius),
                        ops.createString("biome_fade"), ops.createFloat(biomeFade),
                        ops.createString("density_noise_scale"), ops.createInt(densityNoiseScale),
                        ops.createString("density_noise_min"), ops.createFloat(densityNoiseMin),
                        ops.createString("density_noise_max"), ops.createFloat(densityNoiseMax)
                ))
        );
    }

    public static PoissonConfig deserialize(Dynamic<?> dynamic) {
        int radius = dynamic.get("radius").asInt(4);
        float biomeFade = dynamic.get("biome_fade").asFloat(0.2F);
        int variance = dynamic.get("density_noise_scale").asInt(0);
        float min = dynamic.get("density_noise_min").asFloat(0F);
        float max = dynamic.get("density_noise_max").asFloat(1F);
        return new PoissonConfig(radius, biomeFade, variance, min, max);
    }

    private static Module mult(Module a, Module b) {
        if (a == Source.ONE) {
            return b;
        }
        if (b == Source.ONE) {
            return a;
        }
        return a.mult(b);
    }
}
