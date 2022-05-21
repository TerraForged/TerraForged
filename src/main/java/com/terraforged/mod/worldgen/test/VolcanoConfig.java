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

package com.terraforged.mod.worldgen.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record VolcanoConfig(double density, double jitter, Range radius0, Range radius1, Range radius2, Range height0, Range height1, Range fluidLevel) implements FeatureConfiguration {
    public static final Codec<VolcanoConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("density").forGetter(VolcanoConfig::density),
            Codec.DOUBLE.fieldOf("jitter").forGetter(VolcanoConfig::jitter),
            Range.CODEC.fieldOf("pool_radius").forGetter(VolcanoConfig::radius0),
            Range.CODEC.fieldOf("mouth_radius").forGetter(VolcanoConfig::radius1),
            Range.CODEC.fieldOf("base_radius").forGetter(VolcanoConfig::radius2),
            Range.CODEC.fieldOf("pool_height").forGetter(VolcanoConfig::height0),
            Range.CODEC.fieldOf("mouth_height").forGetter(VolcanoConfig::height1),
            Range.CODEC.fieldOf("fluid_level").forGetter(VolcanoConfig::fluidLevel)
    ).apply(instance, VolcanoConfig::new));

    public boolean validBiome(Holder<Biome> biome) {
        return true;
    }

    public double scale() {
        return radius2.max * 1.0;
    }

    public record Range(int min, int max) {
        public static final Codec<Range> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("min").forGetter(Range::min),
                Codec.INT.fieldOf("max").forGetter(Range::max)
        ).apply(instance, Range::new));

        public double get(double rand) {
            return Mth.lerp(rand, min, max);
        }
    }

    public static VolcanoConfig defaultConfig() {
        return new VolcanoConfig(
                // Properties
                1.0,
                0.8,
                // Radii
                new Range(5, 15),
                new Range(20, 30),
                new Range(100, 200),
                // Levels
                new Range(40, 60),
                new Range(100, 200),
                new Range(70, 80));
    }
}