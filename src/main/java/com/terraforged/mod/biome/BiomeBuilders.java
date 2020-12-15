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

package com.terraforged.mod.biome;

import com.terraforged.fm.matcher.dynamic.DynamicMatcher;
import com.terraforged.mod.biome.utils.BiomeBuilder;
import com.terraforged.mod.biome.utils.BiomeUtils;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraftforge.common.BiomeManager;

public class BiomeBuilders {

    public static BiomeBuilder bryce() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.BADLANDS);
        builder.type(BiomeManager.BiomeType.DESERT);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        // dead bush
        DefaultBiomeFeatures.withDesertDeadBushes(builder.getSettings());
        return builder;
    }

    public static BiomeBuilder coldMarsh() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.SNOWY_TAIGA);
        builder.type(BiomeManager.BiomeType.ICY);
        builder.downfall(0.05F);
        builder.temperature(0.15F);
        builder.category(Biome.Category.SWAMP);
        builder.precipitation(Biome.RainType.SNOW);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        deadBush(builder);
        denseGrass(builder);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder coldSteppe() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.GIANT_SPRUCE_TAIGA);
        builder.type(BiomeManager.BiomeType.COOL);
        builder.precipitation(Biome.RainType.SNOW);
        builder.filterFeatures(
                DynamicMatcher.config(BaseTreeFeatureConfig.class),
                DynamicMatcher.feature(Feature.FOREST_ROCK),
                // red/brown mushrooms
                DynamicMatcher.of(Features.RED_MUSHROOM_TAIGA),
                DynamicMatcher.of(Features.BROWN_MUSHROOM_TAIGA)
        );
        builder.temperature(0.25F);
        builder.downfall(0.05F);
        deadBush(builder);
        denseGrass(builder);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder firForest() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.TAIGA);
        builder.type(BiomeManager.BiomeType.COOL);
        deadBush(builder);
        denseGrass(builder);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder flowerPlains() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.FLOWER_FOREST);
        builder.type(BiomeManager.BiomeType.COOL);
        return builder;
    }

    public static BiomeBuilder frozenLake() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.FROZEN_RIVER);
        builder.type(BiomeManager.BiomeType.ICY);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        return builder;
    }

    public static BiomeBuilder frozenMarsh() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.SNOWY_TAIGA);
        builder.type(BiomeManager.BiomeType.ICY);
        builder.category(Biome.Category.SWAMP);
        builder.downfall(0.15F);
        builder.temperature(0.14999F);
        builder.precipitation(Biome.RainType.SNOW);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        return builder;
    }

    public static BiomeBuilder lake() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.RIVER);
        builder.type(BiomeManager.BiomeType.COOL);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        return builder;
    }

    public static BiomeBuilder marshland() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.SWAMP);
        builder.type(BiomeManager.BiomeType.COOL);
        builder.temperature(0.7F);
        builder.category(Biome.Category.SWAMP);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        deadBush(builder);
        ferns(builder);
        denseGrass(builder);
        DefaultBiomeFeatures.withSwampSugarcaneAndPumpkin(builder.getSettings());
        return builder;
    }

    public static BiomeBuilder savannaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.SAVANNA);
        builder.type(BiomeManager.BiomeType.WARM);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        deadBush(builder);
        denseGrass(builder);
        return builder;
    }

    public static BiomeBuilder shatteredSavannaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.SHATTERED_SAVANNA);
        builder.type(BiomeManager.BiomeType.WARM);
        deadBush(builder);
        denseGrass(builder);
        return builder;
    }

    public static BiomeBuilder snowyFirForest() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.SNOWY_TAIGA);
        builder.type(BiomeManager.BiomeType.ICY);
        builder.category(Biome.Category.ICY);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder snowyTaigaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.SNOWY_TAIGA);
        builder.type(BiomeManager.BiomeType.ICY);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder steppe() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.GIANT_SPRUCE_TAIGA);
        builder.filterFeatures(
                DynamicMatcher.config(BaseTreeFeatureConfig.class),
                DynamicMatcher.feature(Feature.FOREST_ROCK),
                // red/brown mushrooms
                DynamicMatcher.of(Features.RED_MUSHROOM_TAIGA),
                DynamicMatcher.of(Features.BROWN_MUSHROOM_TAIGA)
        );
        builder.setParentKey(Biomes.SHATTERED_SAVANNA_PLATEAU);
        builder.category(Biome.Category.SAVANNA);
        builder.copyAmbience(Biomes.SAVANNA);
        builder.downfall(0.05F);
        builder.temperature(1.2F);
        deadBush(builder);
        denseGrass(builder);
        return builder;
    }

    public static BiomeBuilder stoneForest() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.JUNGLE);
        builder.type(BiomeManager.BiomeType.WARM);
        builder.weight(2);
        return builder;
    }

    public static BiomeBuilder taigaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.TAIGA);
        builder.type(BiomeManager.BiomeType.COOL);
        builder.filterFeatures(
                DynamicMatcher.config(BaseTreeFeatureConfig.class),
                // red/brown mushrooms
                DynamicMatcher.of(Features.RED_MUSHROOM_TAIGA),
                DynamicMatcher.of(Features.BROWN_MUSHROOM_TAIGA)
        );
        deadBush(builder);
        denseGrass(builder);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder warmBeach() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.WARM_OCEAN);
        builder.type(BiomeManager.BiomeType.WARM);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        builder.category(Biome.Category.BEACH);
        builder.temperature(1.0F);
        return builder;
    }

    private static void deadBush(BiomeBuilder builder) {
        // dead bush
        DefaultBiomeFeatures.withDesertDeadBushes(builder.getSettings());
    }

    private static void denseGrass(BiomeBuilder builder) {
        // plains grass
        builder.getSettings().withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_PLAIN);
        // extra grass 1
        DefaultBiomeFeatures.withNormalGrassPatch(builder.getSettings());
        // extra grass 2
        DefaultBiomeFeatures.withSavannaGrass(builder.getSettings());
    }

    private static void ferns(BiomeBuilder builder) {
        DefaultBiomeFeatures.withLargeFern(builder.getSettings());
    }
}
