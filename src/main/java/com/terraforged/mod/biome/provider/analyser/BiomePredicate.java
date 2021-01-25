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

package com.terraforged.mod.biome.provider.analyser;

import com.terraforged.engine.world.biome.BiomeData;
import com.terraforged.mod.biome.context.TFBiomeContext;
import net.minecraft.world.biome.Biome;

import java.util.function.BiPredicate;

public interface BiomePredicate {

    boolean test(BiomeData data, Biome biome);

    default boolean test(BiomeData data, TFBiomeContext context) {
        return test(data, context.biomes.get(data.id));
    }

    default BiomePredicate and(BiomePredicate other) {
        return (d, b) -> this.test(d, b) && other.test(d, b);
    }

    default BiomePredicate not(BiomePredicate other) {
        return (d, b) -> this.test(d, b) && !other.test(d, b);
    }

    default BiomePredicate or(BiomePredicate other) {
        return (d, b) -> this.test(d, b) || other.test(d, b);
    }

    static BiomePredicate name(String... name) {
        return (d, b) -> anyMatch(d.name, name, String::contains);
    }

    static BiomePredicate type(Biome.Category... categories) {
        return (d, b) -> anyMatch(b.getCategory(), categories, (c1, c2) -> c1 == c2);
    }

    static BiomePredicate rain(double min, double max) {
        return (d, b) -> d.rainfall >= min && d.rainfall <= max;
    }

    static BiomePredicate rainType(Biome.RainType... rainTypes) {
        return (d, b) -> anyMatch(b.getPrecipitation(), rainTypes, (c1, c2) -> c1 == c2);
    }

    static BiomePredicate temp(double min, double max) {
        return (d, b) -> d.temperature >= min && d.temperature <= max;
    }

    static BiomePredicate depth(double min, double max) {
        return (d, b) -> b.getDepth() >= min && b.getDepth() <= max;
    }

    static <T> boolean anyMatch(T value, T[] test, BiPredicate<T, T> tester) {
        for (T t : test) {
            if (tester.test(value, t)) {
                return true;
            }
        }
        return false;
    }

    BiomePredicate OCEAN = type(Biome.Category.OCEAN);
    BiomePredicate BEACH = type(Biome.Category.BEACH).or(name("beach", "shore"));
    BiomePredicate COAST = type(Biome.Category.MUSHROOM).or(name("coast"));
    BiomePredicate COLD_STEPPE = name("steppe").and(temp(-1, 0.3));
    BiomePredicate DESERT = type(Biome.Category.DESERT).or(temp(0.9, 2).and(rain(-1, 0.2)));
    BiomePredicate GRASSLAND = type(Biome.Category.PLAINS);
    BiomePredicate LAKE = type(Biome.Category.RIVER).and(name("lake")).or(name("lake"));
    BiomePredicate MESA = type(Biome.Category.MESA);
    BiomePredicate MOUNTAIN = type(Biome.Category.EXTREME_HILLS).or(name("mountain"));
    BiomePredicate VOLCANO = name("volcano");
    BiomePredicate RIVER = type(Biome.Category.RIVER).not(LAKE);
    BiomePredicate SAVANNA = type(Biome.Category.SAVANNA).or(temp(0.8, 2).and(rain(-1, 0.4)));
    BiomePredicate STEPPE = name("steppe").and(temp(0.3, 1));
    BiomePredicate TAIGA = type(Biome.Category.TAIGA).or(temp(0.19, 0.35)).not(rainType(Biome.RainType.SNOW));
    BiomePredicate TEMPERATE_FOREST = type(Biome.Category.FOREST).and(rain(-1, 0.81));
    BiomePredicate TEMPERATE_RAINFOREST = type(Biome.Category.FOREST).and(rain(0.8, 2));
    BiomePredicate TROPICAL_RAINFOREST = type(Biome.Category.JUNGLE);
    BiomePredicate TUNDRA = type(Biome.Category.ICY).or(temp(-1, 0.21).and(rainType(Biome.RainType.SNOW)));
    BiomePredicate WETLAND = type(Biome.Category.SWAMP);
}
