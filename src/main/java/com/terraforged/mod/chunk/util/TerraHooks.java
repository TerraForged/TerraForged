package com.terraforged.mod.chunk.util;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.surface.SurfaceManager;
import com.terraforged.mod.feature.manager.FeatureManager;
import com.terraforged.mod.feature.manager.data.DataManager;
import com.terraforged.mod.feature.manager.matcher.biome.BiomeMatcher;
import com.terraforged.mod.feature.manager.matcher.feature.FeatureMatcher;
import com.terraforged.mod.feature.manager.modifier.FeatureModifiers;
import com.terraforged.mod.feature.manager.predicate.DeepWater;
import com.terraforged.mod.feature.manager.predicate.FeaturePredicate;
import com.terraforged.mod.feature.manager.predicate.MinDepth;
import com.terraforged.mod.feature.manager.predicate.MinHeight;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.surface.IcebergsSurface;
import com.terraforged.mod.biome.surface.SteppeSurface;
import com.terraforged.mod.biome.surface.SwampSurface;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.decorator.terrain.BedrockDecorator;
import com.terraforged.mod.decorator.terrain.CoastDecorator;
import com.terraforged.mod.decorator.terrain.ErosionDecorator;
import com.terraforged.mod.decorator.terrain.GeologyDecorator;
import com.terraforged.mod.decorator.feature.LayerDecorator;
import com.terraforged.mod.decorator.feature.SnowEroder;
import com.terraforged.mod.feature.Matchers;
import com.terraforged.mod.feature.BlockDataManager;
import com.terraforged.mod.material.geology.GeoManager;
import com.terraforged.mod.util.setup.SetupHooks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.Feature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TerraHooks {

    public static DataManager createDataManager() {
        return FeatureManager.data(new File("config/terraforged/datapacks"));
    }

    public static List<ColumnDecorator> createBaseDecorators(GeoManager geoManager, TerraContext context) {
        List<ColumnDecorator> processors = new ArrayList<>();
        if (context.terraSettings.features.strataDecorator) {
            Log.info(" - Geology decorator enabled");
            processors.add(new GeologyDecorator(geoManager));
        }
        if (context.terraSettings.features.erosionDecorator) {
            Log.info(" - Erosion decorator enabled");
            processors.add(new ErosionDecorator(context));
        }
        processors.add(new CoastDecorator(context));
        processors.add(new BedrockDecorator(context));
        return processors;
    }

    public static List<ColumnDecorator> createFeatureDecorators(TerraContext context) {
        List<ColumnDecorator> processors = new ArrayList<>();
        if (context.terraSettings.features.naturalSnowDecorator) {
            Log.info(" - Natural snow decorator enabled");
            processors.add(new SnowEroder(context));
        }
        if (context.terraSettings.features.smoothLayerDecorator) {
            Log.info(" - Smooth layer decorator enabled");
            processors.add(new LayerDecorator(context.materials.getLayerManager()));
        }
        return processors;
    }

    public static BlockDataManager createBlockDataManager(DataManager data, TerraContext context) {
        return new BlockDataManager(data);
    }

    public static FeatureManager createFeatureManager(DataManager data, TerraContext context) {
        FeatureModifiers modifiers = FeatureManager.modifiers(data, context.terraSettings.features.customBiomeFeatures);

        if (context.terraSettings.features.strataDecorator) {
            // block stone blobs if strata enabled
            modifiers.getPredicates().add(Matchers.stoneBlobs(), FeaturePredicate.DENY);
        }

        if (!context.terraSettings.features.vanillaWaterFeatures) {
            // block lakes and springs if not enabled
            modifiers.getPredicates().add(FeatureMatcher.of(Feature.LAKE), FeaturePredicate.DENY);
            modifiers.getPredicates().add(FeatureMatcher.of(Feature.SPRING_FEATURE), FeaturePredicate.DENY);
        }

        if (context.terraSettings.features.customBiomeFeatures) {
            // remove default trees from river biomes since forests can go up to the edge of rivers
            modifiers.getPredicates().add(BiomeMatcher.of(Biome.Category.RIVER), Matchers.tree(), FeaturePredicate.DENY);
        }

        // block ugly features
        modifiers.getPredicates().add(Matchers.sedimentDisks(), FeaturePredicate.DENY);

        // limit to deep oceans
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.SHIPWRECK), MinDepth.DEPTH55);
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.OCEAN_RUIN), DeepWater.INSTANCE);
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.OCEAN_MONUMENT), DeepWater.INSTANCE);

        // prevent mineshafts above ground
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.MINESHAFT), MinHeight.HEIGHT80);

        return FeatureManager.create(context.world, SetupHooks.setup(modifiers, context.copy()));
    }

    public static SurfaceManager createSurfaceManager(TerraContext context) {
        SurfaceManager manager = new SurfaceManager();
        manager.replace(Biomes.FROZEN_OCEAN.delegate.get(), new IcebergsSurface(context, 20, 15));
        manager.replace(Biomes.DEEP_FROZEN_OCEAN.delegate.get(), new IcebergsSurface(context, 30, 30));
        manager.replace(Biomes.SWAMP.delegate.get(), new SwampSurface());
        manager.replace(ModBiomes.MARSHLAND, new SwampSurface());
        manager.replace(ModBiomes.STEPPE, new SteppeSurface());
        return SetupHooks.setup(manager, context);
    }

    public static GeoManager createGeologyManager(TerraContext context) {
        return new GeoManager(context);
    }
}
