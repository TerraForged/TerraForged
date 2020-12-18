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

import com.terraforged.mod.api.biome.surface.SurfaceManager;
import com.terraforged.mod.api.chunk.column.ColumnDecorator;
import com.terraforged.engine.tile.gen.TileCache;
import com.terraforged.mod.featuremanager.FeatureManager;
import com.terraforged.mod.featuremanager.data.DataManager;
import com.terraforged.mod.featuremanager.structure.FMStructureManager;
import com.terraforged.mod.feature.BlockDataManager;
import com.terraforged.mod.material.geology.GeoManager;
import com.terraforged.mod.util.setup.SetupHooks;

import java.util.List;
import java.util.function.Function;

public class GeneratorResources {

    final TileCache tileCache;
    final FeatureManager featureManager;
    final BlockDataManager blockDataManager;
    final GeoManager geologyManager;
    final FMStructureManager structureManager;
    final SurfaceManager surfaceManager;
    final List<ColumnDecorator> surfaceDecorators;
    final List<ColumnDecorator> postProcessors;

    public GeneratorResources(TFChunkGenerator generator, TerraContext context) {
        this.surfaceManager = SetupFactory.createSurfaceManager(context);
        this.structureManager = SetupFactory.createStructureManager(context);
        this.geologyManager = SetupFactory.createGeologyManager(context);
        this.surfaceDecorators = SetupFactory.createSurfaceDecorators(context);
        this.postProcessors = SetupFactory.createFeatureDecorators(context);

        try (DataManager data = SetupFactory.createDataManager()) {
            FeatureManager.initData(data);
            this.featureManager = SetupFactory.createFeatureManager(data, context, generator);
            this.blockDataManager = SetupFactory.createBlockDataManager(data, context);
            FeatureManager.clearData();
        }

        this.tileCache = context.cache.get();

        SetupHooks.setup(context.materials.get().layerManager, context.copy());
        SetupHooks.setup(surfaceDecorators, postProcessors, context.copy());
    }

    public static Function<TerraContext, GeneratorResources> factory(TFChunkGenerator generator) {
        return context -> new GeneratorResources(generator, context);
    }
}
