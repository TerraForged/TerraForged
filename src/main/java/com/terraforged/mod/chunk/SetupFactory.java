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

package com.terraforged.mod.chunk;

import com.terraforged.mod.Log;
import com.terraforged.mod.api.biome.surface.SurfaceManager;
import com.terraforged.mod.api.chunk.column.ColumnDecorator;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.surface.BryceSurface;
import com.terraforged.mod.biome.surface.DesertSurface;
import com.terraforged.mod.biome.surface.ForestSurface;
import com.terraforged.mod.biome.surface.IcebergsSurface;
import com.terraforged.mod.biome.surface.StoneForestSurface;
import com.terraforged.mod.biome.surface.SwampSurface;
import com.terraforged.mod.chunk.column.ErosionDecorator;
import com.terraforged.mod.chunk.column.post.LayerDecorator;
import com.terraforged.mod.chunk.column.post.SnowEroder;
import com.terraforged.mod.feature.BlockDataManager;
import com.terraforged.mod.feature.Matchers;
import com.terraforged.mod.feature.VolcanoPredicate;
import com.terraforged.mod.feature.feature.FreezeLayer;
import com.terraforged.mod.featuremanager.FeatureManager;
import com.terraforged.mod.featuremanager.data.DataManager;
import com.terraforged.mod.featuremanager.matcher.biome.BiomeMatcher;
import com.terraforged.mod.featuremanager.matcher.feature.FeatureMatcher;
import com.terraforged.mod.featuremanager.modifier.FeatureModifiers;
import com.terraforged.mod.featuremanager.predicate.FeaturePredicate;
import com.terraforged.mod.featuremanager.transformer.FeatureTransformer;
import com.terraforged.mod.material.Materials;
import com.terraforged.mod.material.geology.GeoManager;
import com.terraforged.mod.util.setup.SetupHooks;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.Feature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SetupFactory {

    public static DataManager createDataManager() {
        return DataManager.of(new File("config/terraforged/datapacks"));
    }

    public static List<ColumnDecorator> createSurfaceDecorators(TerraContext context) {
        List<ColumnDecorator> processors = new ArrayList<>();
        if (test("Erosion decorator", context.terraSettings.miscellaneous.erosionDecorator)) {
            processors.add(new ErosionDecorator(context));
        }
        return processors;
    }

    public static List<ColumnDecorator> createFeatureDecorators(TerraContext context) {
        List<ColumnDecorator> processors = new ArrayList<>();
        if (test("Natural snow decorator", context.terraSettings.miscellaneous.naturalSnowDecorator)) {
            processors.add(new SnowEroder(context));
        }
        if (test("Smooth layer decorator", context.terraSettings.miscellaneous.smoothLayerDecorator)) {
            processors.add(new LayerDecorator(context.materials.then(Materials::getLayerManager)));
        }
        return processors;
    }

    public static BlockDataManager createBlockDataManager(DataManager data, TerraContext context) {
        return new BlockDataManager(data);
    }

    public static FeatureManager createFeatureManager(DataManager data, TerraContext context, TFChunkGenerator generator) {
        FeatureModifiers modifiers = FeatureManager.modifiers(data, context.terraSettings.miscellaneous.customBiomeFeatures, context.biomeContext);

        if (context.terraSettings.miscellaneous.strataDecorator) {
            // block stone blobs if strata enabled
            modifiers.getPredicates().add("STONE_BLOBS", Matchers.stoneBlobs(), FeaturePredicate.DENY);
        }

        if (testInv("Vanilla lakes", !context.terraSettings.miscellaneous.vanillaLakes)) {
            // block lakes if not enabled
            modifiers.getPredicates().add("LAKES", Matchers.lakes(), FeaturePredicate.DENY);
        }

        if (testInv("Vanilla lava lakes", !context.terraSettings.miscellaneous.vanillaLavaLakes)) {
            // block lava-lakes if not enabled
            modifiers.getPredicates().add("LAVA_LAKES", Matchers.lavaLakes(), FeaturePredicate.DENY);
        }

        if (testInv("Vanilla water springs", !context.terraSettings.miscellaneous.vanillaSprings)) {
            // block springs if not enabled
            modifiers.getPredicates().add("WATER_SPRINGS", FeatureMatcher.and(Feature.SPRING, Blocks.WATER), FeaturePredicate.DENY);
        }

        if (testInv("Vanilla lava springs", !context.terraSettings.miscellaneous.vanillaLavaSprings)) {
            // block springs if not enabled
            modifiers.getPredicates().add("LAVA_SPRINGS", FeatureMatcher.and(Feature.SPRING, Blocks.LAVA), FeaturePredicate.DENY);
        }

        if (test("Custom features", context.terraSettings.miscellaneous.customBiomeFeatures)) {
            // remove default trees from river biomes since forests can go up to the edge of rivers
            modifiers.getPredicates().add("RIVER_TREE", BiomeMatcher.of(context.biomeContext, Biome.Category.RIVER), Matchers.tree(), FeaturePredicate.DENY);

            // places snow layers below and on top of trees
            modifiers.getTransformers().add(
                    "SNOW_LAYERS",
                    BiomeMatcher.ANY,
                    FeatureMatcher.of(Feature.FREEZE_TOP_LAYER),
                    FeatureTransformer.replace(Feature.FREEZE_TOP_LAYER, FreezeLayer.INSTANCE)
            );

            // reduce dead bushes in deserts/badlands
            modifiers.getTransformers().add(
                    "DESERT_DEAD_BUSHES",
                    BiomeMatcher.of(context.biomeContext, Biome.Category.MESA, Biome.Category.DESERT),
                    Matchers.deadBush(),
                    FeatureTransformer.builder().key("tries", 1).build()
            );
        }
        // prevent trees growing at high elevation on volcanoes
        modifiers.getPredicates().add("VOLCANO_TREELINE", Matchers.allTrees(), new VolcanoPredicate(generator));

        return FeatureManager.create(SetupHooks.setup(modifiers, context));
    }

    public static SurfaceManager createSurfaceManager(TerraContext context) {
        SurfaceManager manager = new SurfaceManager(context.biomeContext);
        manager.replace(Biomes.DEEP_FROZEN_OCEAN, new IcebergsSurface(context, 30, 30));
        manager.replace(Biomes.FROZEN_OCEAN, new IcebergsSurface(context, 20, 15));
        manager.append(ModBiomes.BRYCE, new BryceSurface(context.seed(BryceSurface.SEED_OFFSET)));
        manager.append(ModBiomes.STONE_FOREST, new StoneForestSurface(context.seed(StoneForestSurface.SEED_OFFSET)));
        manager.append(
                new DesertSurface(context),
                Biomes.DESERT,
                Biomes.DESERT_HILLS,
                Biomes.DESERT_LAKES
        );
        manager.replace(
                new SwampSurface(context),
                Biomes.SWAMP,
                ModBiomes.MARSHLAND
        );
        manager.append(
                new ForestSurface(context),
                Biomes.FOREST,
                Biomes.WOODED_HILLS,
                Biomes.DARK_FOREST,
                Biomes.DARK_FOREST_HILLS
        );
        return SetupHooks.setup(manager, context);
    }

    public static GeoManager createGeologyManager(TerraContext context) {
        return SetupHooks.setup(new GeoManager(context), context);
    }

    private static String getEnabled(boolean flag) {
        return flag ? "enabled" : "disabled";
    }

    private static boolean test(String message, boolean flag) {
        Log.info(" - {} {}", message, getEnabled(flag));
        return flag;
    }

    private static boolean testInv(String message, boolean flag) {
        test(message, !flag);
        return flag;
    }
}
