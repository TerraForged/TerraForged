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

package com.terraforged.mod.feature;

import com.terraforged.fm.GameContext;
import com.terraforged.fm.matcher.BiomeFeatureMatcher;
import com.terraforged.fm.matcher.biome.BiomeMatcher;
import com.terraforged.fm.matcher.feature.FeatureMatcher;
import com.terraforged.mod.feature.feature.DiskFeature;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;

public class Matchers {

    public static FeatureMatcher stoneBlobs() {
        return FeatureMatcher.builder()
                .or("minecraft:ore").and("minecraft:dirt")
                .or("minecraft:ore").and("minecraft:gravel")
                .or("minecraft:ore").and("minecraft:granite")
                .or("minecraft:ore").and("minecraft:diorite")
                .or("minecraft:ore").and("minecraft:andesite")
                .build();
    }

    public static FeatureMatcher tree() {
        return FeatureMatcher.builder()
                .or(Blocks.OAK_LOG).and(Blocks.OAK_LEAVES)
                .build();
    }

    public static BiomeFeatureMatcher sedimentDisks(GameContext context) {
        return new BiomeFeatureMatcher(
                BiomeMatcher.of(context, Biome.Category.RIVER, Biome.Category.SWAMP),
                FeatureMatcher.builder()
                        .or(Feature.DISK).and("minecraft:sand")
                        .or(Feature.DISK).and("minecraft:dirt")
                        .or(DiskFeature.INSTANCE).and("minecraft:sand")
                        .or(DiskFeature.INSTANCE).and("minecraft:dirt")
                        .build()
        );
    }
}
