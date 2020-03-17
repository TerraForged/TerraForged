/*
 *
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

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.api.chunk.surface.ChunkSurfaceBuffer;
import com.terraforged.api.chunk.surface.SurfaceContext;
import com.terraforged.api.chunk.surface.SurfaceManager;
import com.terraforged.api.material.layer.LayerManager;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.RegionCache;
import com.terraforged.core.region.RegionGenerator;
import com.terraforged.core.region.Size;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.core.world.decorator.Decorator;
import com.terraforged.feature.FeatureManager;
import com.terraforged.feature.matcher.dynamic.DynamicMatcher;
import com.terraforged.feature.matcher.feature.FeatureMatcher;
import com.terraforged.feature.modifier.FeatureModifierLoader;
import com.terraforged.feature.modifier.FeatureModifiers;
import com.terraforged.feature.predicate.DeepWater;
import com.terraforged.feature.predicate.FeaturePredicate;
import com.terraforged.feature.predicate.MinHeight;
import com.terraforged.feature.template.type.FeatureTypes;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.BiomeProvider;
import com.terraforged.mod.chunk.fix.ChunkCarverFix;
import com.terraforged.mod.chunk.fix.RegionFix;
import com.terraforged.mod.decorator.ChunkPopulator;
import com.terraforged.mod.decorator.base.BedrockDecorator;
import com.terraforged.mod.decorator.base.CoastDecorator;
import com.terraforged.mod.decorator.base.ErosionDecorator;
import com.terraforged.mod.decorator.base.GeologyDecorator;
import com.terraforged.mod.decorator.feature.LayerDecorator;
import com.terraforged.mod.decorator.feature.SnowEroder;
import com.terraforged.mod.decorator.surface.FrozenOcean;
import com.terraforged.mod.feature.Matchers;
import com.terraforged.mod.feature.TerrainHelper;
import com.terraforged.mod.feature.predicate.TreeLine;
import com.terraforged.mod.material.Materials;
import com.terraforged.mod.material.geology.GeoManager;
import com.terraforged.mod.util.setup.SetupHooks;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.Feature;

import java.util.ArrayList;
import java.util.List;

public class TerraChunkGenerator extends ObfHelperChunkGenerator<ChunkGeneratorConfig> implements ChunkProcessor {

    private final TerraContext context;
    private final BiomeProvider biomeProvider;
    private final TerrainHelper terrainHelper;

    private final GeoManager geologyManager;
    private final FeatureManager featureManager;
    private final SurfaceManager surfaceManager;
    private final List<ColumnDecorator> baseDecorators;
    private final List<ColumnDecorator> postProcessors;

    private final RegionCache regionCache;

    public TerraChunkGenerator(TerraContext context, BiomeProvider biomeProvider, ChunkGeneratorConfig settings) {
        super(context.world, biomeProvider, settings);
        this.context = context;
        this.biomeProvider = biomeProvider;
        this.surfaceManager = SetupHooks.setup(createSurfaceManager(), context.copy());
        this.geologyManager = SetupHooks.setup(createGeologyManager(context), context.copy());
        this.baseDecorators = createBaseDecorators(context);
        this.postProcessors = createFeatureDecorators(context);
        this.terrainHelper = new TerrainHelper((int) world.getSeed(), 0.8F);
        this.featureManager = createFeatureManager(context);
        this.regionCache = createRegionCache(context);
        SetupHooks.setup(getLayerManager(), context.copy());
        SetupHooks.setup(baseDecorators, postProcessors, context.copy());
    }

    @Override
    public void setStructureStarts(BiomeAccess biomeAccess, Chunk chunk, ChunkGenerator<?> chunkGenerator, StructureManager structureManager) {
        super.setStructureStarts(biomeAccess, chunk, this, structureManager);
    }

    @Override
    public final void populateBiomes(Chunk chunk) {
        ChunkPos pos = chunk.getPos();
        ChunkReader reader = getChunkReader(pos.x, pos.z);
        TerraBiomeArray container = getBiomeSource().createBiomeContainer(reader);
        ((ProtoChunk) chunk).setBiomes(container);
        // apply chunk-local heightmap modifications
        preProcess(pos, reader, container);
    }

    @Override
    public final void preProcess(ChunkPos pos, ChunkReader chunk, TerraBiomeArray container) {
        chunk.iterate((cell, dx, dz) -> {
            Biome biome = container.getBiome(dx, dz);
            for (Decorator decorator : getBiomeSource().getDecorators(biome)) {
                if (decorator.apply(cell, pos.getStartX() + dx, pos.getStartZ() + dz)) {
                    return;
                }
            }
        });
    }

    @Override
    public final void populateNoise(IWorld world, Chunk chunk) {
        DecoratorContext context = new DecoratorContext(chunk, getContext().levels, getContext().terrain, getContext().factory.getClimate());
        TerraBiomeArray container = getBiomeContainer(chunk);
        container.getChunkReader().iterate((cell, dx, dz) -> {
            int px = context.blockX + dx;
            int pz = context.blockZ + dz;
            int py = (int) (cell.value * getMaxY());
            context.cell = cell;
            context.biome = container.getBiome(dx, dz);
            ChunkPopulator.INSTANCE.decorate(chunk, context, px, py, pz);
        });
        terrainHelper.flatten(world, chunk, context.blockX, context.blockZ);
    }

    @Override
    public final void buildSurface(ChunkRegion world, Chunk chunk) {
        ChunkSurfaceBuffer buffer = new ChunkSurfaceBuffer(chunk);
        SurfaceContext context = getContext().surface(buffer, getConfig());
        TerraBiomeArray container = getBiomeContainer(chunk);
        container.getChunkReader().iterate((cell, dx, dz) -> {
            int px = context.blockX + dx;
            int pz = context.blockZ + dz;
            int top = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, dx, dz) + 1;

            buffer.setSurfaceLevel(top);

            context.cell = cell;
            context.biome = container.getBiome(dx, dz);
            context.noise = getSurfaceNoise(px, pz) * 15D;

            getSurfaceManager().getSurface(context).buildSurface(px, pz, top, context);

            int py = (int) (cell.value * getMaxY());
            for (ColumnDecorator processor : getBaseDecorators()) {
                processor.decorate(buffer, context, px, py, pz);
            }
        });
    }

    @Override
    public void carve(BiomeAccess biomeAccess, Chunk chunk, GenerationStep.Carver carver) {
        super.carve(biomeAccess, new ChunkCarverFix(chunk, context.materials), carver);
    }

    @Override
    public void generateFeatures(ChunkRegion region) {
        int chunkX = region.getCenterChunkX();
        int chunkZ = region.getCenterChunkZ();
        Chunk chunk = region.getChunk(chunkX, chunkZ);
        TerraBiomeArray container = getBiomeContainer(chunk);
        Biome biome = container.getFeatureBiome();
        DecoratorContext context = getContext().decorator(chunk);

        IWorld regionFix = new RegionFix(region, this);
        BlockPos pos = new BlockPos(context.blockX, 0, context.blockZ);

        // place biome features
        featureManager.decorate(this, regionFix, chunk, biome, pos);

        // run post processes on chunk
        postProcess(container.getChunkReader(), container, context);

        // bake biome array & discard gen data
        ((ProtoChunk) chunk).setBiomes(container.bakeBiomes());
    }

    @Override
    public final void postProcess(ChunkReader chunk, TerraBiomeArray container, DecoratorContext context) {
        chunk.iterate((cell, dx, dz) -> {
            int px = context.blockX + dx;
            int pz = context.blockZ + dz;
            int py = context.chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, dx, dz);
            context.cell = cell;
            context.biome = container.getBiome(dx, dz);
            for (ColumnDecorator decorator : getPostProcessors()) {
                decorator.decorate(context.chunk, context, px, py, pz);
            }
        });
    }

    @Override
    public int getHeightOnGround(int x, int z, Heightmap.Type heightmapType) {
        return this.sampleHeight(x, z, heightmapType); // todo may be wrong
    }

    @Override
    public int sampleHeight(int x, int z, Heightmap.Type type) {
        int chunkX = Size.blockToChunk(x);
        int chunkZ = Size.blockToChunk(z);
        ChunkReader chunk = getChunkReader(chunkX, chunkZ);
        Cell<?> cell = chunk.getCell(x, z);
        return (int) (cell.value * getMaxY());
    }

    @Override
    public BiomeProvider getBiomeSource() {
        return biomeProvider;
    }

    @Override
    public final int getMaxY() {
        return getContext().levels.worldHeight;
    }

    @Override
    public final int getSeaLevel() {
        return getContext().levels.waterLevel;
    }

    @Override
    public int getSpawnHeight() {
        return getContext().levels.groundLevel;
    }

    public final TerraContext getContext() {
        return context;
    }

    public final Materials getMaterials() {
        return context.materials;
    }

    public final GeoManager getGeologyManager() {
        return geologyManager;
    }

    public final LayerManager getLayerManager() {
        return context.materials.getLayerManager();
    }

    public final SurfaceManager getSurfaceManager() {
        return surfaceManager;
    }

    public final List<ColumnDecorator> getBaseDecorators() {
        return baseDecorators;
    }

    public final List<ColumnDecorator> getPostProcessors() {
        return postProcessors;
    }

    protected TerraBiomeArray getBiomeContainer(Chunk chunk) {
        if (chunk.getBiomeArray() instanceof TerraBiomeArray) {
            return (TerraBiomeArray) chunk.getBiomeArray();
        }

        ChunkReader view = getChunkReader(chunk.getPos().x, chunk.getPos().z);
        TerraBiomeArray container = getBiomeSource().createBiomeContainer(view);
        if (chunk instanceof ProtoChunk) {
            ((ProtoChunk) chunk).setBiomes(container);
        }

        return container;
    }

    protected FeatureManager createFeatureManager(TerraContext context) {
        FeatureModifiers modifiers;
        if (context.terraSettings.features.customBiomeFeatures) {
            Log.info(" - Custom biome features enabled");
            modifiers = FeatureModifierLoader.load();
        } else {
            modifiers = new FeatureModifiers();
        }

        // block ugly features
        modifiers.getPredicates().add(Matchers.STONE_BLOBS, FeaturePredicate.DENY);
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.DISK), FeaturePredicate.DENY);
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.LAKE), FeaturePredicate.DENY);
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.SPRING_FEATURE), FeaturePredicate.DENY);

        // limit to deep oceans
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.SHIPWRECK), DeepWater.INSTANCE);
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.OCEAN_RUIN), DeepWater.INSTANCE);
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.OCEAN_MONUMENT), DeepWater.INSTANCE);

        // prevent mineshafts above ground
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.MINESHAFT), MinHeight.HEIGHT80);

        // prevent trees/bamboo growing too high up
        TreeLine treeLine = new TreeLine(context);
        modifiers.getPredicates().add(FeatureTypes.TREE.matcher(), treeLine);
        modifiers.getPredicates().add(FeatureMatcher.of(Feature.BAMBOO), treeLine);
        modifiers.getDynamic().add(DynamicMatcher.feature(AbstractTreeFeature.class), treeLine);

        return FeatureManager.create(context.world, SetupHooks.setup(modifiers, context.copy()));
    }

    protected GeoManager createGeologyManager(TerraContext context) {
        return new GeoManager(context);
    }

    protected SurfaceManager createSurfaceManager() {
        SurfaceManager manager = new SurfaceManager();
        manager.replace(Biomes.FROZEN_OCEAN, new FrozenOcean(context, 20, 15));
        manager.replace(Biomes.DEEP_FROZEN_OCEAN, new FrozenOcean(context, 30, 30));
        return manager;
    }

    protected List<ColumnDecorator> createBaseDecorators(TerraContext context) {
        List<ColumnDecorator> processors = new ArrayList<>();
        if (context.terraSettings.features.strataDecorator) {
            Log.info(" - Geology decorator enabled");
            processors.add(new GeologyDecorator(geologyManager));
        }
        if (context.terraSettings.features.erosionDecorator) {
            Log.info(" - Erosion decorator enabled");
            processors.add(new ErosionDecorator(context));
        }
        processors.add(new CoastDecorator(context));
        processors.add(new BedrockDecorator());
        return processors;
    }

    protected List<ColumnDecorator> createFeatureDecorators(TerraContext context) {
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

    protected RegionCache createRegionCache(TerraContext context) {
        return RegionGenerator.builder()
                .legacy(context.terraSettings.version == 0)
                .pool(ThreadPool.getFixed())
                .factory(context.factory)
                .size(3, 2)
                .build()
                .toCache();
    }

    public ChunkReader getChunkReader(int chunkX, int chunkZ) {
        return regionCache.getChunk(chunkX, chunkZ);
    }
}
