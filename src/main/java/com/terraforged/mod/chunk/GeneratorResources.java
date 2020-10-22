package com.terraforged.mod.chunk;

import com.terraforged.api.biome.surface.SurfaceManager;
import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.core.tile.gen.TileCache;
import com.terraforged.fm.FeatureManager;
import com.terraforged.fm.data.DataManager;
import com.terraforged.fm.structure.FMStructureManager;
import com.terraforged.mod.feature.BlockDataManager;
import com.terraforged.mod.material.geology.GeoManager;
import com.terraforged.mod.util.setup.SetupHooks;

import java.util.List;

public class GeneratorResources {

    final TileCache tileCache;
    final FeatureManager featureManager;
    final BlockDataManager blockDataManager;
    final GeoManager geologyManager;
    final FMStructureManager structureManager;
    final SurfaceManager surfaceManager;
    final List<ColumnDecorator> surfaceDecorators;
    final List<ColumnDecorator> postProcessors;

    public GeneratorResources(TerraContext context) {
        this.surfaceManager = TerraSetupFactory.createSurfaceManager(context);
        this.structureManager = TerraSetupFactory.createStructureManager(context);
        this.geologyManager = TerraSetupFactory.createGeologyManager(context);
        this.surfaceDecorators = TerraSetupFactory.createSurfaceDecorators(context);
        this.postProcessors = TerraSetupFactory.createFeatureDecorators(context);

        try (DataManager data = TerraSetupFactory.createDataManager()) {
            FeatureManager.initData(data);
            this.featureManager = TerraSetupFactory.createFeatureManager(data, context);
            this.blockDataManager = TerraSetupFactory.createBlockDataManager(data, context);
            FeatureManager.clearData();
        }

        this.tileCache = context.cache.get();

        SetupHooks.setup(context.materials.get().layerManager, context.copy());
        SetupHooks.setup(surfaceDecorators, postProcessors, context.copy());
    }
}
