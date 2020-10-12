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

package com.terraforged.mod.data.gen.feature;

import com.terraforged.fm.data.FeatureInjectorProvider;
import com.terraforged.fm.matcher.biome.BiomeMatcher;
import com.terraforged.fm.matcher.feature.FeatureMatcher;
import com.terraforged.fm.transformer.FeatureTransformer;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;

public class Ores {

    public static void addInjectors(FeatureInjectorProvider provider) {
        provider.add("ores/coal", FeatureMatcher.and(Blocks.COAL_ORE, Feature.ORE), scaledOre(140, 128, 20));
        provider.add("ores/iron", FeatureMatcher.and(Blocks.IRON_ORE, Feature.ORE), scaledOre(100, 64, 24));
        provider.add("ores/gold", FeatureMatcher.and(Blocks.GOLD_ORE, Feature.ORE, 2), scaledOre(35, 32, 2));
        provider.add("ores/redstone", FeatureMatcher.and(Blocks.REDSTONE_ORE, Feature.ORE), scaledOre(18, 16, 8));
        provider.add("ores/diamond", FeatureMatcher.and(Blocks.DIAMOND_ORE, Feature.ORE), FeatureTransformer.key("maximum", 18));
        provider.add(
                "ores/gold_extra",
                BiomeMatcher.of(provider.getContext(), Biome.Category.MESA),
                FeatureMatcher.and(Blocks.GOLD_ORE, Feature.ORE, 20),
                FeatureTransformer.builder()
                        .key("bottom_offset", 35)
                        .key("top_offset", 35)
                        .key("maximum", 100)
                        .key("count", volumeScale(35, 100, 32, 80, 20))
                        .build()
        );
    }

    private static FeatureTransformer scaledOre(int newHeight, int vanillaHeight, int vanillaCount) {
        int newCount = volumeScale(0, newHeight, 0, vanillaHeight, vanillaCount);
        return FeatureTransformer.builder().key("maximum", newHeight).key("count", newCount).build();
    }

    private static int volumeScale(int newMin, int newMax, int oldMin, int oldMax, int vanillaCount) {
        float newVolume = (newMax - newMin) * 16 * 16;
        float oldVolume = (oldMax - oldMin) * 16 * 16;
        float scale = newVolume / oldVolume;
        return Math.round(vanillaCount * scale);
    }
}
