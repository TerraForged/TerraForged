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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.api.biome.surface.SurfaceManager;
import com.terraforged.api.chunk.column.BlockColumn;
import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.material.WGTags;
import com.terraforged.api.material.layer.LayerManager;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.tile.Size;
import com.terraforged.core.tile.Tile;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.core.tile.gen.TileCache;
import com.terraforged.fm.FeatureManager;
import com.terraforged.fm.data.DataManager;
import com.terraforged.fm.structure.FMStructureManager;
import com.terraforged.fm.util.codec.Codecs;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.biome.utils.StructureLocator;
import com.terraforged.mod.chunk.generator.*;
import com.terraforged.mod.feature.BlockDataManager;
import com.terraforged.mod.material.Materials;
import com.terraforged.mod.material.geology.GeoManager;
import com.terraforged.mod.util.setup.SetupHooks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TerraChunkGenerator extends ChunkGenerator {

    public static final Codec<TerraChunkGenerator> CODEC = Codecs.create(TerraChunkGenerator::encodeGenerator, TerraChunkGenerator::decodeGenerator);

    private final long seed;
    private final TerraContext context;
    private final DimensionSettings settings;
    private final TerraBiomeProvider biomeProvider;

    private final Generator.Mobs mobGenerator;
    private final Generator.Biomes biomeGenerator;
    private final Generator.Carvers terrainCarver;
    private final Generator.Terrain terrainGenerator;
    private final Generator.Surfaces surfaceGenerator;
    private final Generator.Features featureGenerator;
    private final Generator.Structures structureGenerator;

    private final GeoManager geologyManager;
    private final FeatureManager featureManager;
    private final FMStructureManager structureManager;
    private final SurfaceManager surfaceManager;
    private final BlockDataManager blockDataManager;
    private final List<ColumnDecorator> surfaceDecorators;
    private final List<ColumnDecorator> postProcessors;

    private final TileCache tileCache;

    protected TerraChunkGenerator(long seed, TerraBiomeProvider biomeProvider, DimensionSettings settings) {
        super(biomeProvider, settings.getStructures());

        this.settings = settings;
        this.biomeProvider = biomeProvider;

        this.seed = seed;
        this.context = null;
        this.mobGenerator = null;
        this.biomeGenerator = null;
        this.terrainCarver = null;
        this.terrainGenerator = null;
        this.surfaceGenerator = null;
        this.featureGenerator = null;
        this.structureGenerator = null;
        this.geologyManager = null;
        this.featureManager = null;
        this.structureManager = null;
        this.surfaceManager = null;
        this.blockDataManager = null;
        this.surfaceDecorators = Collections.emptyList();
        this.postProcessors = Collections.emptyList();
        this.tileCache = null;
    }

    public TerraChunkGenerator(TerraBiomeProvider biomeProvider, DimensionSettings settings) {
        super(biomeProvider, settings.getStructures());
        TerraContext context = biomeProvider.getContext();

        this.seed = context.terraSettings.world.seed;
        this.context = context;
        this.settings = settings;
        this.biomeProvider = biomeProvider;
        this.mobGenerator = new MobGenerator(this);
        this.biomeGenerator = new BiomeGenerator(this);
        this.terrainCarver = new TerrainCarver(this);
        this.terrainGenerator = new BaseGenerator(this);
        this.surfaceGenerator = new SurfaceGenerator(this);
        this.featureGenerator = new FeatureGenerator(this);
        this.structureGenerator = new StructureGenerator(this);

        this.surfaceManager = TerraSetupFactory.createSurfaceManager(context);
        this.structureManager = TerraSetupFactory.createStructureManager(context);
        this.geologyManager = TerraSetupFactory.createGeologyManager(context);
        this.surfaceDecorators = TerraSetupFactory.createSurfaceDecorators(context);
        this.postProcessors = TerraSetupFactory.createFeatureDecorators(context);
        this.tileCache = context.cache.get();

        try (DataManager data = TerraSetupFactory.createDataManager()) {
            FeatureManager.initData(data);
            this.featureManager = TerraSetupFactory.createFeatureManager(data, context);
            this.blockDataManager = TerraSetupFactory.createBlockDataManager(data, context);
            FeatureManager.clearData();
        }

        SetupHooks.setup(getLayerManager(), context.copy());
        SetupHooks.setup(surfaceDecorators, postProcessors, context.copy());
    }

    private TerraChunkGenerator create(long seed) {
        Log.debug("Creating seeded generator: {}", seed);
        TerraBiomeProvider biomes = getBiomeProvider().getBiomeProvider(seed);
        return new TerraChunkGenerator(biomes, getSettings());
    }

    public long getSeed() {
        return seed;
    }

    public DimensionSettings getSettings() {
        return settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> func_230347_a_() {
        return CODEC;
    }

    @Override
    public ChunkGenerator func_230349_a_(long p_230349_1_) {
        return create(seed);
    }

    @Override // getBlockColumn
    public IBlockReader func_230348_a_(int x, int z) {
        float value;
        try (ChunkReader chunkReader = getChunkReader(x >> 4, z >> 4)) {
            value = chunkReader.getCell(x, z).value;
        }

        int height = getContext().levels.scale(value) + 1;
        int surface = Math.max(height, getSeaLevel() + 1);
        BlockColumn column = GeneratorResources.get().column.withCapacity(surface);
        BlockState solid = settings.getDefaultBlock();
        for (int y = 0; y < height; y++) {
            column.set(y, solid);
        }

        BlockState fluid = settings.getDefaultFluid();
        for (int y = height; y < surface; y++) {
            column.set(y, fluid);
        }

        return column;
    }

    @Nullable // findStructure
    public BlockPos func_235956_a_(ServerWorld world, Structure<?> structure, BlockPos pos, int attempts, boolean flag) {
        if (!this.biomeProvider.hasStructure(structure)) {
            return null;
        }
        if (structure == Structure.field_236375_k_) {
            return super.func_235956_a_(world, structure, pos, attempts, flag);
        }
        StructureSeparationSettings settings = this.settings.getStructures().func_236197_a_(structure);
        if (settings == null) {
            return null;
        }
        return StructureLocator.findStructure(this, world, world.func_241112_a_(), structure, pos, attempts, flag, settings);
    }

    @Override
    public final void func_242707_a(DynamicRegistries registries, StructureManager structures, IChunk chunk, TemplateManager templates, long seed) {
        structureGenerator.generateStructureStarts(chunk, registries, structures, templates);
    }

    @Override
    public final void func_235953_a_(ISeedReader world, StructureManager structures, IChunk chunk) {
        structureGenerator.generateStructureReferences(world, chunk, structures);
    }

    @Override
    public final void func_242706_a(Registry<Biome> registry, IChunk chunk) {
        biomeGenerator.generateBiomes(chunk);
    }

    @Override
    public final void func_230352_b_(IWorld world, StructureManager structures, IChunk chunk) {
        terrainGenerator.generateTerrain(world, chunk, structures);
    }

    @Override
    public final void generateSurface(WorldGenRegion world, IChunk chunk) {
        surfaceGenerator.generateSurface(world, chunk);
    }

    @Override
    public final void func_230350_a_(long seed, BiomeManager biomes, IChunk chunk, GenerationStage.Carving carver) {
        terrainCarver.carveTerrain(biomes, chunk, carver);
    }

    @Override
    public final void func_230351_a_(WorldGenRegion region, StructureManager structures) {
        featureGenerator.generateFeatures(region, structures);
    }

    @Override
    public final void func_230354_a_(WorldGenRegion region) {
        mobGenerator.generateMobs(region);
    }

    @Override
    public final List<MobSpawnInfo.Spawners> func_230353_a_(Biome biome, StructureManager structures, EntityClassification type, BlockPos pos) {
        return mobGenerator.getSpawns(biome, structures, type, pos);
    }

    @Override
    public final int getHeight(int x, int z, Heightmap.Type type) {
        int chunkX = Size.blockToChunk(x);
        int chunkZ = Size.blockToChunk(z);
        try (ChunkReader chunk = getChunkReader(chunkX, chunkZ)) {
            Cell cell = chunk.getCell(x, z);
            int level = context.levels.scale(cell.value) + 1;
            if (type == Heightmap.Type.OCEAN_FLOOR || type == Heightmap.Type.OCEAN_FLOOR_WG) {
                return level;
            }
            return Math.max(getSeaLevel(), level);
        }
    }

    @Override
    public TerraBiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    @Override
    public final int func_230355_e_() {
        // getMaxHeight
        return getWorldHeight();
    }

    @Override
    public final int func_230356_f_() {
        // getSeaLevel
        return getSeaLevel();
    }

    @Override
    public final int getGroundHeight() {
        return getContext().levels.groundLevel;
    }

    public final int getWorldHeight() {
        return getContext().levels.worldHeight;
    }

    public final int getSeaLevel() {
        return getContext().levels.waterLevel;
    }

    public final TerraContext getContext() {
        return context;
    }

    public final Materials getMaterials() {
        return context.materials.get();
    }

    public final FeatureManager getFeatureManager() {
        return featureManager;
    }

    public final FMStructureManager getStructureManager() {
        return structureManager;
    }

    public final GeoManager getGeologyManager() {
        return geologyManager;
    }

    public final LayerManager getLayerManager() {
        return getMaterials().getLayerManager();
    }

    public final SurfaceManager getSurfaceManager() {
        return surfaceManager;
    }

    public final BlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public final List<ColumnDecorator> getSurfaceDecorators() {
        return surfaceDecorators;
    }

    public final List<ColumnDecorator> getPostProcessors() {
        return postProcessors;
    }

    public final void queueChunk(int chunkX, int chunkZ) {
        int rx = tileCache.chunkToRegion(chunkX);
        int rz = tileCache.chunkToRegion(chunkZ);
        tileCache.queueRegion(rx, rz);
    }

    public final Tile getTile(int chunkX, int chunkZ) {
        int rx = tileCache.chunkToRegion(chunkX);
        int rz = tileCache.chunkToRegion(chunkZ);
        return tileCache.getRegion(rx, rz);
    }

    public final ChunkReader getChunkReader(int chunkX, int chunkZ) {
        return tileCache.getChunk(chunkX, chunkZ);
    }

    public static ChunkReader getChunk(IWorld world, ChunkGenerator generator) {
        if (generator instanceof TerraChunkGenerator) {
            TerraChunkGenerator terra = (TerraChunkGenerator) generator;
            if (world instanceof IChunk) {
                IChunk chunk = (IChunk) world;
                return terra.getChunkReader(chunk.getPos().x, chunk.getPos().z);
            }

            if (world instanceof WorldGenRegion) {
                WorldGenRegion region = (WorldGenRegion) world;
                return terra.getChunkReader(region.getMainChunkX(), region.getMainChunkZ());
            }
        }
        throw new IllegalStateException("NONONO");
    }

    public static TerraChunkGenerator createDummy(long seed, TerraBiomeProvider biomes, DimensionSettings settings) {
        return new TerraChunkGenerator(seed, biomes, settings);
    }

    private static <T> Dynamic<T> encodeGenerator(TerraChunkGenerator generator, DynamicOps<T> ops) {
        T biomeProvider = Codecs.encodeAndGet(TerraBiomeProvider.CODEC, generator.getBiomeProvider(), ops);
        T dimensionSettings = Codecs.encodeAndGet(DimensionSettings.field_236097_a_, generator.getSettings(), ops);
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("biome_provider"), biomeProvider,
                ops.createString("dimension_settings"), dimensionSettings
        )));
    }

    private static <T> TerraChunkGenerator decodeGenerator(Dynamic<T> dynamic) {
        TerraBiomeProvider biomes = Codecs.decodeAndGet(TerraBiomeProvider.CODEC, dynamic.get("biome_provider"));
        DimensionSettings settings = Codecs.decodeAndGet(DimensionSettings.field_236097_a_, dynamic.get("dimension_settings"));
        return new TerraChunkGenerator(biomes, settings);
    }
}
