package com.terraforged.mod.chunk;

import com.terraforged.mod.Log;
import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.biome.surface.SurfaceManager;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.surface.BriceSurface;
import com.terraforged.mod.biome.surface.DesertSurface;
import com.terraforged.mod.biome.surface.StoneForestSurface;
import com.terraforged.mod.biome.surface.ForestSurface;
import com.terraforged.mod.biome.surface.IcebergsSurface;
import com.terraforged.mod.biome.surface.SwampSurface;
import com.terraforged.mod.chunk.column.BedrockDecorator;
import com.terraforged.mod.chunk.column.ErosionDecorator;
import com.terraforged.mod.chunk.column.GeologyDecorator;
import com.terraforged.mod.chunk.column.post.LayerDecorator;
import com.terraforged.mod.chunk.column.post.SnowEroder;
import com.terraforged.mod.feature.BlockDataManager;
import com.terraforged.mod.feature.Matchers;
import com.terraforged.mod.feature.feature.FreezeLayer;
import com.terraforged.fm.FeatureManager;
import com.terraforged.fm.data.DataManager;
import com.terraforged.fm.matcher.biome.BiomeMatcher;
import com.terraforged.fm.matcher.feature.FeatureMatcher;
import com.terraforged.fm.modifier.FeatureModifiers;
import com.terraforged.fm.predicate.DeepWater;
import com.terraforged.fm.predicate.FeaturePredicate;
import com.terraforged.fm.predicate.MinHeight;
import com.terraforged.fm.structure.StructureManager;
import com.terraforged.fm.transformer.FeatureTransformer;
import com.terraforged.mod.material.geology.GeoManager;
import com.terraforged.mod.util.setup.SetupHooks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TerraSetupFactory {

    public static DataManager createDataManager() {
        return FeatureManager.data(new File("config/terraforged/datapacks"));
    }

    public static List<ColumnDecorator> createBaseDecorators(GeoManager geoManager, TerraContext context) {
        List<ColumnDecorator> processors = new ArrayList<>();
        if (context.terraSettings.miscellaneous.strataDecorator) {
            Log.info(" - Geology decorator enabled");
            processors.add(new GeologyDecorator(geoManager));
        }
        if (context.terraSettings.miscellaneous.erosionDecorator) {
            Log.info(" - Erosion decorator enabled");
            processors.add(new ErosionDecorator(context));
        }
        processors.add(new BedrockDecorator(context));
        return processors;
    }

    public static List<ColumnDecorator> createFeatureDecorators(TerraContext context) {
        List<ColumnDecorator> processors = new ArrayList<>();
        if (context.terraSettings.miscellaneous.naturalSnowDecorator) {
            Log.info(" - Natural snow decorator enabled");
            processors.add(new SnowEroder(context));
        }
        if (context.terraSettings.miscellaneous.smoothLayerDecorator) {
            Log.info(" - Smooth layer decorator enabled");
            processors.add(new LayerDecorator(context.materials.getLayerManager()));
        }
        return processors;
    }

    public static BlockDataManager createBlockDataManager(DataManager data, TerraContext context) {
        return new BlockDataManager(data);
    }

    public static FeatureManager createFeatureManager(DataManager data, TerraContext context) {
        FeatureModifiers modifiers = FeatureManager.modifiers(data, context.terraSettings.miscellaneous.customBiomeFeatures);

        if (context.terraSettings.miscellaneous.strataDecorator) {
            // block stone blobs if strata enabled
            modifiers.getPredicates().add(Matchers.stoneBlobs(), FeaturePredicate.DENY);
        }

        if (!context.terraSettings.miscellaneous.vanillaWaterFeatures) {
            // block lakes and springs if not enabled
            modifiers.getPredicates().add(FeatureMatcher.of(Feature.LAKE), FeaturePredicate.DENY);
            modifiers.getPredicates().add(FeatureMatcher.of(Feature.SPRING_FEATURE), FeaturePredicate.DENY);
        }

        if (context.terraSettings.miscellaneous.customBiomeFeatures) {
            // remove default trees from river biomes since forests can go up to the edge of rivers
            modifiers.getPredicates().add(BiomeMatcher.of(Biome.Category.RIVER), Matchers.tree(), FeaturePredicate.DENY);

            // places snow layers below and on top of trees
            modifiers.getTransformers().add(
                    BiomeMatcher.ANY,
                    FeatureMatcher.of(Feature.FREEZE_TOP_LAYER),
                    FeatureTransformer.replace(Feature.FREEZE_TOP_LAYER, FreezeLayer.INSTANCE)
            );
        }

        // block ugly features
        modifiers.getPredicates().add(Matchers.sedimentDisks(), FeaturePredicate.DENY);
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.MINESHAFT), new MinHeight(context.levels.waterY + 20));

        return FeatureManager.create(context.world, SetupHooks.setup(modifiers, context.copy()));
    }

    public static SurfaceManager createSurfaceManager(TerraContext context) {
        SurfaceManager manager = new SurfaceManager();
        manager.replace(Biomes.DEEP_FROZEN_OCEAN, new IcebergsSurface(context, 30, 30));
        manager.replace(Biomes.FROZEN_OCEAN, new IcebergsSurface(context, 20, 15));
        manager.append(ModBiomes.BRYCE, new BriceSurface(context.seed));
        manager.append(ModBiomes.ERODED_PINNACLE, new StoneForestSurface(context.seed));
        manager.append(
                new DesertSurface(context),
                Biomes.DESERT,
                Biomes.DESERT_HILLS,
                Biomes.DESERT_LAKES
        );
        manager.replace(
                new SwampSurface(),
                Biomes.SWAMP.delegate.get(),
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

    public static StructureManager createStructureManager(TerraContext context) {
        StructureManager manager = new StructureManager();
        manager.register(Structure.OCEAN_MONUMENT, DeepWater.INSTANCE);
        manager.register(Structure.OCEAN_RUIN, DeepWater.INSTANCE);
        return SetupHooks.setup(manager, context);
    }

    public static GeoManager createGeologyManager(TerraContext context) {
        return new GeoManager(context);
    }
}
