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

import com.google.gson.JsonObject;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.feature.feature.DiskFeature;
import com.terraforged.mod.featuremanager.matcher.BiomeFeatureMatcher;
import com.terraforged.mod.featuremanager.matcher.biome.BiomeMatcher;
import com.terraforged.mod.featuremanager.matcher.feature.FeatureMatcher;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;

public class Matchers {

    public static FeatureMatcher stoneBlobs() {
        return FeatureMatcher.builder()
                // Match ore features when the target is using a tag_match on base_stone_overworld and the placed block is the material we want to exclude
                .or("minecraft:ore").and("minecraft:tag_match").and("minecraft:base_stone_overworld").and("state", state("minecraft:dirt"))
                .or("minecraft:ore").and("minecraft:tag_match").and("minecraft:base_stone_overworld").and("state", state("minecraft:gravel"))
                .or("minecraft:ore").and("minecraft:tag_match").and("minecraft:base_stone_overworld").and("state", state("minecraft:granite"))
                .or("minecraft:ore").and("minecraft:tag_match").and("minecraft:base_stone_overworld").and("state", state("minecraft:diorite"))
                .or("minecraft:ore").and("minecraft:tag_match").and("minecraft:base_stone_overworld").and("state", state("minecraft:andesite"))
                .build();
    }

    public static FeatureMatcher deadBush() {
        return FeatureMatcher.of(Blocks.DEAD_BUSH);
    }

    public static FeatureMatcher tree() {
        return FeatureMatcher.builder()
                .or(Blocks.OAK_LOG).and(Blocks.OAK_LEAVES)
                .build();
    }

    public static FeatureMatcher terraTrees() {
        return FeatureMatcher.builder()
                .or(TerraFeatures.INSTANCE)
                .or(Blocks.OAK_LOG).and(Blocks.OAK_LEAVES)
                .or(Blocks.ACACIA_LOG).and(Blocks.ACACIA_LEAVES)
                .or(Blocks.BIRCH_LOG).and(Blocks.BIRCH_LEAVES)
                .or(Blocks.DARK_OAK_LOG).and(Blocks.DARK_OAK_LEAVES)
                .or(Blocks.JUNGLE_LOG).and(Blocks.JUNGLE_LEAVES)
                .or(Blocks.SPRUCE_LOG).and(Blocks.SPRUCE_LEAVES)
                .build();
    }

    public static FeatureMatcher allTrees() {
        return FeatureMatcher.builder()
                // is tree
                .or(TerraFeatures.INSTANCE).newRule()
                // has trunk & foliage placers & places logs & leaves
                .or("minecraft:trunk_placer").and("minecraft:foliage_placer").and(BlockTags.LOGS).and(BlockTags.LEAVES).build();
    }

    public static BiomeFeatureMatcher sedimentDisks(TFBiomeContext context) {
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

    private static JsonObject state(String stoneType) {
        JsonObject state = new JsonObject();
        state.addProperty("Name", stoneType);
        return state;
    }
}
