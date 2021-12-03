/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.worldgen.asset;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.util.DataUtil;
import com.terraforged.mod.util.seed.ContextSeedable;
import com.terraforged.mod.worldgen.biome.viability.*;

import java.util.function.Supplier;

public record ViabilityConfig(Supplier<BiomeTag> biomes, float density, Viability viability) implements ContextSeedable<ViabilityConfig> {
    public static final ViabilityConfig NONE = new ViabilityConfig(Suppliers.ofInstance(BiomeTag.NONE), 1F, Viability.NONE);

    public static final Codec<ViabilityConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeTag.CODEC.fieldOf("biomes").forGetter(ViabilityConfig::biomes),
            Codec.FLOAT.optionalFieldOf("density", 1F).forGetter(ViabilityConfig::density),
            ViabilityCodec.CODEC.fieldOf("viability").forGetter(ViabilityConfig::viability)
    ).apply(instance, ViabilityConfig::new));

    @Override
    public ViabilityConfig withSeed(long seed) {
        var viability = withSeed(seed, viability(), Viability.class);
        return new ViabilityConfig(biomes, density, viability);
    }

    static {
        DataUtil.registerSub(Viability.class, MultViability.SPEC);
        DataUtil.registerSub(Viability.class, HeightViability.SPEC);
        DataUtil.registerSub(Viability.class, NoiseViability.SPEC);
        DataUtil.registerSub(Viability.class, SlopeViability.SPEC);
        DataUtil.registerSub(Viability.class, SumViability.SPEC);
    }
}
