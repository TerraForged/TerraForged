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

package com.terraforged.mod.biome.provider.analyser;

import com.terraforged.mod.biome.context.TFBiomeContext;
import net.minecraft.world.biome.Biome;

import java.util.function.BiPredicate;

public interface BiomePredicate {

    boolean test(int biome, TFBiomeContext context);

    default BiomePredicate and(BiomePredicate other) {
        return (b, c) -> this.test(b, c) && other.test(b, c);
    }

    default BiomePredicate not(BiomePredicate other) {
        return (b, c) -> this.test(b, c) && !other.test(b, c);
    }

    default BiomePredicate or(BiomePredicate other) {
        return (b, c) -> this.test(b, c) || other.test(b, c);
    }

    static BiomePredicate name(String... name) {
        return (b, c) -> anyMatch(c.getName(b), name, String::contains);
    }

    static BiomePredicate type(Biome.Category... categories) {
        return (b, c) -> anyMatch(c.getProperties().getProperty(b, Biome::getBiomeCategory), categories, (x, y) -> x == y);
    }

    static BiomePredicate rain(float min, float max) {
        return (b, c) -> testRange(c.getProperties().getMoisture(b), min,  max);
    }

    static BiomePredicate rainType(Biome.RainType... rainTypes) {
        return (b, c) -> anyMatch(c.getProperties().getProperty(b, Biome::getPrecipitation), rainTypes, (x, y) -> x == y);
    }

    static BiomePredicate temp(float min, float max) {
        return (b, c) -> {
            float temp = c.getProperties().getTemperature(b);
            return testRange(temp, min,  max);
        };
    }

    static BiomePredicate depth(float min, float max) {
        return (b, c) -> testRange(c.getProperties().getDepth(b), min, max);
    }

    static <T> boolean anyMatch(T value, T[] test, BiPredicate<T, T> tester) {
        for (T t : test) {
            if (tester.test(value, t)) {
                return true;
            }
        }
        return false;
    }

    static boolean testRange(float value, float min, float max) {
        return value >= min && value <= max;
    }

    float ANY_MIN = -Float.MAX_VALUE;
    float ANY_MAX = Float.MAX_VALUE;

    // Temps
    float FROZEN = -0.4F;
    float COLD = 0.2F;
    float WARM = 1.4F;
    float HOT = 1.7F;

    // Rain
    float LIGHT = 0.2F;
    float MODERATE = 0.4F;
    float HEAVY = 0.8F;

    BiomePredicate OCEAN = type(Biome.Category.OCEAN);
    BiomePredicate BEACH = type(Biome.Category.BEACH).or(name("beach", "shore"));
    BiomePredicate COAST = type(Biome.Category.MUSHROOM).or(name("coast"));
    BiomePredicate COLD_STEPPE = name("steppe").and(temp(ANY_MIN, COLD));
    BiomePredicate DESERT = type(Biome.Category.DESERT).or(temp(HOT, ANY_MAX).and(rain(ANY_MIN, LIGHT)));
    BiomePredicate GRASSLAND = type(Biome.Category.PLAINS);
    BiomePredicate LAKE = type(Biome.Category.RIVER).and(name("lake")).or(name("lake"));
    BiomePredicate MESA = type(Biome.Category.MESA);
    BiomePredicate MOUNTAIN = type(Biome.Category.EXTREME_HILLS).or(name("mountain")).or(name("cliff"));
    BiomePredicate VOLCANO = name("volcano").or(name("volcanic"));
    BiomePredicate RIVER = type(Biome.Category.RIVER).not(LAKE);
    BiomePredicate SAVANNA = type(Biome.Category.SAVANNA).or(temp(WARM, ANY_MAX).and(rain(ANY_MIN, MODERATE)));
    BiomePredicate STEPPE = name("steppe").and(temp(COLD, ANY_MAX));
    BiomePredicate TAIGA = type(Biome.Category.TAIGA).or(temp(FROZEN, COLD)).not(rainType(Biome.RainType.SNOW));
    BiomePredicate TEMPERATE_FOREST = type(Biome.Category.FOREST).and(rain(ANY_MIN, HEAVY));
    BiomePredicate TEMPERATE_RAINFOREST = type(Biome.Category.FOREST).and(rain(HEAVY, ANY_MAX));
    BiomePredicate TROPICAL_RAINFOREST = type(Biome.Category.JUNGLE);
    BiomePredicate TUNDRA = type(Biome.Category.ICY).or(temp(ANY_MIN, FROZEN).and(rainType(Biome.RainType.SNOW)));
    BiomePredicate WETLAND = type(Biome.Category.SWAMP);
}
