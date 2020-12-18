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

package com.terraforged.mod.chunk;

import com.terraforged.mod.Log;
import com.terraforged.mod.api.biome.surface.SurfaceManager;
import com.terraforged.mod.api.chunk.column.ColumnDecorator;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.surface.*;
import com.terraforged.mod.biome.utils.Structures;
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
import com.terraforged.mod.featuremanager.predicate.*;
import com.terraforged.mod.featuremanager.structure.FMStructureManager;
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
        return FeatureManager.data(new File("config/terraforged/datapacks"));
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
        FeatureModifiers modifiers = FeatureManager.modifiers(data, context.terraSettings.miscellaneous.customBiomeFeatures, context.gameContext);

        if (context.terraSettings.miscellaneous.strataDecorator) {
            // block stone blobs if strata enabled
            modifiers.getPredicates().add(Matchers.stoneBlobs(), FeaturePredicate.DENY);
        }

        if (testInv("Vanilla lakes", !context.terraSettings.miscellaneous.vanillaLakes)) {
            // block lakes if not enabled
            modifiers.getPredicates().add(FeatureMatcher.and(Feature.LAKE, Blocks.WATER), FeaturePredicate.DENY);
        }

        if (testInv("Vanilla lava lakes", !context.terraSettings.miscellaneous.vanillaLavaLakes)) {
            // block lava-lakes if not enabled
            modifiers.getPredicates().add(FeatureMatcher.and(Feature.LAKE, Blocks.LAVA), FeaturePredicate.DENY);
        }

        if (testInv("Vanilla springs", !context.terraSettings.miscellaneous.vanillaSprings)) {
            // block springs if not enabled
            modifiers.getPredicates().add(FeatureMatcher.of(Feature.SPRING_FEATURE), FeaturePredicate.DENY);
        }


        if (test("Custom features", context.terraSettings.miscellaneous.customBiomeFeatures)) {
            // remove default trees from river biomes since forests can go up to the edge of rivers
            modifiers.getPredicates().add(BiomeMatcher.of(context.gameContext, Biome.Category.RIVER), Matchers.tree(), FeaturePredicate.DENY);

            // places snow layers below and on top of trees
            modifiers.getTransformers().add(
                    BiomeMatcher.ANY,
                    FeatureMatcher.of(Feature.FREEZE_TOP_LAYER),
                    FeatureTransformer.replace(Feature.FREEZE_TOP_LAYER, FreezeLayer.INSTANCE)
            );
        }
        // prevent trees growing at high elevation on volcanoes
        modifiers.getPredicates().add(Matchers.trees(), new VolcanoPredicate(generator));

        // reduce dead bushes in deserts/badlands
        modifiers.getTransformers().add(
                BiomeMatcher.of(context.gameContext, Biome.Category.MESA, Biome.Category.DESERT),
                Matchers.deadBush(),
                FeatureTransformer.builder().key("tries", 1).build()
        );

        // block ugly features
        modifiers.getPredicates().add(Matchers.sedimentDisks(context.gameContext), FeaturePredicate.DENY);

        // prevent structures in weird places
        modifiers.add(Structures.shipwreck(), MinDepth.DEPTH55);
        modifiers.add(Structures.oceanRuin(), MinDepth.DEPTH45);
        modifiers.add(Structures.oceanMonument(), DeepWater.INSTANCE);

        modifiers.add(Structures.mansion(), new MaxHeight(context.levels.waterY + 64));
        modifiers.add(Structures.mineshaft(), new MinHeight(context.levels.waterY + 16));

        return FeatureManager.create(SetupHooks.setup(modifiers, context.copy()));
    }

    public static SurfaceManager createSurfaceManager(TerraContext context) {
        SurfaceManager manager = new SurfaceManager(context.gameContext);
        manager.replace(Biomes.DEEP_FROZEN_OCEAN, new IcebergsSurface(context, 30, 30));
        manager.replace(Biomes.FROZEN_OCEAN, new IcebergsSurface(context, 20, 15));
        manager.append(ModBiomes.BRYCE, new BriceSurface(context.seed));
        manager.append(ModBiomes.STONE_FOREST, new StoneForestSurface(context.seed));
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

    public static FMStructureManager createStructureManager(TerraContext context) {
        FMStructureManager manager = new FMStructureManager();
        manager.register(Structures.oceanMonument(), DeepWater.INSTANCE);
        manager.register(Structures.oceanRuin(), DeepWater.INSTANCE);
        manager.register(Structures.shipwreck(), new MinDepth(context.levels.waterLevel - 8));
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
