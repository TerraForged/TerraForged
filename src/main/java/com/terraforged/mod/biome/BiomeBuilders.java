package com.terraforged.mod.biome;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.terraforged.fm.matcher.dynamic.DynamicMatcher;
import com.terraforged.mod.biome.utils.BiomeBuilder;
import com.terraforged.mod.biome.utils.BiomeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraftforge.common.BiomeManager;

import java.util.Optional;

public class BiomeBuilders {

    public static BiomeBuilder bryce() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.BADLANDS);
        builder.type(BiomeManager.BiomeType.DESERT);
        builder.filterFeatures(DynamicMatcher.config(BaseTreeFeatureConfig.class));
        // dead bush
        DefaultBiomeFeatures.func_243705_S(builder.getSettings());
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
                DynamicMatcher.of(Features.field_243826_aY),
                DynamicMatcher.of(Features.field_243826_aY)
        );
        builder.temperature(0.2F);
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
        denseGrass(builder);
        ferns(builder);
        return builder;
    }

    public static BiomeBuilder savannaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.SAVANNA);
        builder.type(BiomeManager.BiomeType.WARM);
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
                DynamicMatcher.of(Features.field_243826_aY),
                DynamicMatcher.of(Features.field_243826_aY)
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
        return builder;
    }

    public static BiomeBuilder taigaScrub() {
        BiomeBuilder builder = BiomeUtils.getBuilder(Biomes.TAIGA);
        builder.type(BiomeManager.BiomeType.COOL);
        builder.filterFeatures(
                DynamicMatcher.config(BaseTreeFeatureConfig.class),
                // red/brown mushrooms
                DynamicMatcher.of(Features.field_243826_aY),
                DynamicMatcher.of(Features.field_243826_aY)
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
        DefaultBiomeFeatures.func_243705_S(builder.getSettings());
    }

    private static void denseGrass(BiomeBuilder builder) {
        // plains grass
        DefaultBiomeFeatures.func_243711_Y(builder.getSettings());
        // extra grass 1
        DefaultBiomeFeatures.func_243696_J(builder.getSettings());
        // extra grass 2
        DefaultBiomeFeatures.func_243698_L(builder.getSettings());
    }

    private static void ferns(BiomeBuilder builder) {
        DefaultBiomeFeatures.func_243757_q(builder.getSettings());
    }
}
