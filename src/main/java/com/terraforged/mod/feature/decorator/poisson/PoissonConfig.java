/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.feature.decorator.poisson;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.engine.util.poisson.PoissonContext;
import com.terraforged.mod.api.feature.decorator.DecorationContext;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.world.gen.placement.IPlacementConfig;

public class PoissonConfig implements IPlacementConfig {

    public static final Codec<PoissonConfig> CODEC = Codecs.create(
            PoissonConfig::serialize,
            PoissonConfig::deserialize
    );

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

    public void apply(DecorationContext decorationContext, PoissonContext context) {
        Module fade = Source.ONE;
        Module density = Source.ONE;

        if (biomeFade > 0.075F) {
            fade = BiomeVariance.of(decorationContext, biomeFade);
        }

        if (densityNoiseScale > 0) {
            density = Source.simplex(context.seed, densityNoiseScale, 1)
                    .scale(densityNoiseMax - densityNoiseMin)
                    .bias(densityNoiseMin);
        }

        context.density = mult(fade, density);
    }

    public static <T> Dynamic<T> serialize(PoissonConfig config, DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(
                ImmutableMap.of(
                        ops.createString("radius"), ops.createInt(config.radius),
                        ops.createString("biome_fade"), ops.createFloat(config.biomeFade),
                        ops.createString("density_noise_scale"), ops.createInt(config.densityNoiseScale),
                        ops.createString("density_noise_min"), ops.createFloat(config.densityNoiseMin),
                        ops.createString("density_noise_max"), ops.createFloat(config.densityNoiseMax)
                ))
        );
    }

    public static <T> PoissonConfig deserialize(Dynamic<T> dynamic) {
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
