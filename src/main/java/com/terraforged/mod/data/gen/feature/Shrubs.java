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

import com.google.gson.JsonPrimitive;
import com.terraforged.mod.featuremanager.FeatureSerializer;
import com.terraforged.mod.featuremanager.data.FeatureInjectorProvider;
import com.terraforged.mod.featuremanager.matcher.biome.BiomeMatcher;
import com.terraforged.mod.featuremanager.matcher.feature.FeatureMatcher;
import com.terraforged.mod.featuremanager.transformer.FeatureAppender;
import com.terraforged.mod.featuremanager.transformer.FeatureInjector;
import com.terraforged.mod.featuremanager.transformer.FeatureReplacer;
import com.terraforged.mod.featuremanager.transformer.FeatureTransformer;
import com.terraforged.mod.feature.feature.BushFeature;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.blockplacer.BlockPlacerType;
import net.minecraft.world.gen.blockplacer.DoublePlantBlockPlacer;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.Placement;

import java.util.Arrays;

public class Shrubs {

    private static final String[] BIRCH = {"minecraft:birch*", "minecraft:tall_birch*"};
    private static final String[] FOREST = {"minecraft:forest", "minecraft:forest_hills", "minecraft:dark_forest", "minecraft:dark_forest_hills"};
    private static final String[] MARSH = {"terraforged:marshland", "terraforged:cold_marshland"};
    private static final String[] PLAINS = {"minecraft:birch*", "minecraft:plains", "minecraft:sunflower_plains", "minecraft:mountains"};
    private static final String[] STEPPE = {"minecraft:savanna", "minecraft:shattered_savanna", "terraforged:steppe"};
    private static final String[] COLD_STEPPE = {"terraforged:cold_steppe", "terraforged:cold_marshland"};
    private static final String[] TAIGA = {
            "minecraft:snowy_tundra",
            "minecraft:taiga",
            "minecraft:taiga_hills",
            "minecraft:wooded_mountains",
            "minecraft:taiga_mountains",
            "minecraft:snowy_taiga_mountains",
            "minecraft:gravelly_mountains",
            "minecraft:modified_gravelly_mountains",
            "terraforged:taiga_scrub",
            "terraforged:snowy_taiga_scrub",
    };

    public static void addInjectors(FeatureInjectorProvider provider) {
        // large bush
        addLargeBush(provider, "shrubs/birch_forest_bush", BIRCH, Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 0, 0.05F, 1);
        addLargeBush(provider, "shrubs/forest_bush", FOREST, Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 1, 0.05F, 1);

        // small bush
        addSmallBush(provider, "shrubs/marsh_bush", MARSH, Blocks.OAK_LOG, Blocks.BIRCH_LEAVES, 0.05F, 0.09F, 0.65F, 0, 0.3F, 1);
        addSmallBush(provider, "shrubs/plains_bush", PLAINS, Blocks.OAK_LOG, Blocks.BIRCH_LEAVES, 0.05F, 0.09F, 0.65F, 0, 0.05F, 1);
        addSmallBush(provider, "shrubs/steppe_bush", STEPPE, Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES, 0.06F, 0.08F, 0.7F, 0, 0.125F, 1);
        addSmallBush(provider, "shrubs/cold_steppe_bush", COLD_STEPPE, Blocks.SPRUCE_LOG, Blocks.OAK_LEAVES, 0.05F, 0.075F, 0.6F, 0, 0.125F, 1);
        addSmallBush(provider, "shrubs/taiga_scrub_bush", TAIGA, Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES, 0.05F, 0.075F, 0.6F, 0, 0.1F, 1);

        // forestFern
        // forestGrass
        // mountainGrass
        addBirchGrass(provider, "shrubs/birch_forest_grass");
    }

    private static void addLargeBush(FeatureInjectorProvider provider, String path, String[] biomes, Block log, Block leaves, int count, float chance, int extra) {
        provider.add(
                path,
                BiomeMatcher.of(provider.getContext(), biomes),
                FeatureMatcher.and(log, leaves),
                FeatureInjector.after(transform(
                        bush(count, chance, extra),
                        FeatureTransformer.builder()
                                .value(wrap(Blocks.JUNGLE_LEAVES), wrap(leaves))
                                .value(wrap(Blocks.JUNGLE_LOG), wrap(log))
                                .build()
                ))
        );
    }

    private static void addSmallBush(FeatureInjectorProvider provider, String path, String[] biomes, Block log, Block leaves, float air, float leaf, float size, int count, float chance, int extra) {
        provider.add(
                path,
                BiomeMatcher.of(provider.getContext(), biomes),
                FeatureMatcher.ANY,
                FeatureAppender.head(
                        GenerationStage.Decoration.VEGETAL_DECORATION,
                        BushFeature.INSTANCE.withConfiguration(new BushFeature.Config(
                                log.getDefaultState(),
                                leaves.getDefaultState(),
                                air,
                                leaf,
                                size
                        )).withPlacement(Placement.COUNT_EXTRA.configure(new AtSurfaceWithExtraConfig(
                                count,
                                chance,
                                extra
                        )))
                )
        );
    }

    private static void addBirchGrass(FeatureInjectorProvider provider, String path) {
        provider.add(
                path,
                BiomeMatcher.of(provider.getContext(), BIRCH),
                FeatureMatcher.and(BlockPlacerType.DOUBLE_PLANT),
                FeatureReplacer.of(
                        Feature.RANDOM_SELECTOR.withConfiguration(new MultipleRandomFeatureConfig(
                                Arrays.asList(
                                        patch(doubleBuilder(Blocks.TALL_GRASS).tries(56).func_227317_b_(), 0.8F),
                                        patch(doubleBuilder(Blocks.LILAC).tries(64).func_227317_b_(), 0.5F),
                                        patch(doubleBuilder(Blocks.LARGE_FERN).tries(48).func_227317_b_(), 0.3F),
                                        patch(singleBuilder(Blocks.FERN).tries(24).func_227317_b_(), 0.2F),
                                        patch(doubleBuilder(Blocks.PEONY).tries(32).func_227317_b_(), 0.1F)
                                ),
                                Feature.RANDOM_PATCH.withConfiguration(doubleBuilder(Blocks.TALL_GRASS).tries(48).func_227317_b_().build())
                        )).withPlacement(Placement.COUNT.configure(new FeatureSpreadConfig(5)))
                )
        );
    }

    private static ConfiguredRandomFeatureList patch(BlockClusterFeatureConfig.Builder cluster, float chance) {
        return Feature.RANDOM_PATCH.withConfiguration(cluster.build()).withChance(chance);
    }

    private static BlockClusterFeatureConfig.Builder singleBuilder(Block block) {
        return new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(block.getDefaultState()), new SimpleBlockPlacer());
    }

    private static BlockClusterFeatureConfig.Builder doubleBuilder(Block block) {
        return new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(block.getDefaultState()), new DoublePlantBlockPlacer());
    }

    private static JsonPrimitive wrap(Block block) {
        return new JsonPrimitive(block.getRegistryName().toString());
    }

    private static ConfiguredFeature<?, ?> bush(int count, float chance, int extra) {
        return bush(Placement.COUNT_EXTRA.configure(new AtSurfaceWithExtraConfig(count, chance, extra)));
    }

    private static ConfiguredFeature<?, ?> bush(ConfiguredPlacement<?> placement) {
        return Features.JUNGLE_BUSH.withPlacement(placement);
    }

    private static ConfiguredFeature<?, ?> transform(ConfiguredFeature<?, ?> feature, FeatureTransformer transformer) {
        return FeatureSerializer.deserializeUnchecked(transformer.apply(FeatureSerializer.serialize(feature)));
    }
}
