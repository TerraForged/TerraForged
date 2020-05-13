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
import com.terraforged.api.chunk.surface.SurfaceManager;
import com.terraforged.api.material.layer.LayerManager;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.Size;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.region.gen.RegionCache;
import com.terraforged.mod.feature.manager.FeatureManager;
import com.terraforged.mod.feature.manager.data.DataManager;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.BiomeProvider;
import com.terraforged.mod.chunk.component.BiomeGenerator;
import com.terraforged.mod.chunk.component.MobGenerator;
import com.terraforged.mod.chunk.component.StructureGenerator;
import com.terraforged.mod.chunk.component.TerrainCarver;
import com.terraforged.mod.chunk.component.TerrainGenerator;
import com.terraforged.mod.chunk.util.TerraHooks;
import com.terraforged.mod.feature.BlockDataManager;
import com.terraforged.mod.material.Materials;
import com.terraforged.mod.material.geology.GeoManager;
import com.terraforged.mod.util.Environment;
import com.terraforged.mod.util.setup.SetupHooks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class TerraChunkGenerator extends ChunkGenerator<GenerationSettings> {

    private final TerraContext context;
    private final BiomeProvider biomeProvider;

    private final MobGenerator mobGenerator;
    private final BiomeGenerator biomeGenerator;
    private final TerrainCarver terrainCarver;
    private final TerrainGenerator terrainGenerator;
    private final StructureGenerator structureGenerator;

    private final GeoManager geologyManager;
    private final FeatureManager featureManager;
    private final SurfaceManager surfaceManager;
    private final BlockDataManager blockDataManager;
    private final List<ColumnDecorator> baseDecorators;
    private final List<ColumnDecorator> postProcessors;

    private final RegionCache regionCache;

    public TerraChunkGenerator(TerraContext context, BiomeProvider biomeProvider, GenerationSettings settings) {
        super(context.world, biomeProvider, settings);
        this.context = context;
        this.biomeProvider = biomeProvider;
        this.mobGenerator = new MobGenerator(this);
        this.biomeGenerator = new BiomeGenerator(this);
        this.terrainCarver = new TerrainCarver(this);
        this.terrainGenerator = new TerrainGenerator(this);
        this.structureGenerator = new StructureGenerator(this);

        this.surfaceManager = TerraHooks.createSurfaceManager(context);
        this.geologyManager = TerraHooks.createGeologyManager(context);
        this.baseDecorators = TerraHooks.createBaseDecorators(geologyManager, context);
        this.postProcessors = TerraHooks.createFeatureDecorators(context);
        this.regionCache = context.cache;

        try (DataManager data = TerraHooks.createDataManager()) {
            FeatureManager.initData(data);
            this.featureManager = TerraHooks.createFeatureManager(data, context);
            this.blockDataManager = TerraHooks.createBlockDataManager(data, context);
            FeatureManager.clearData();
        }

        SetupHooks.setup(getLayerManager(), context.copy());
        SetupHooks.setup(baseDecorators, postProcessors, context.copy());
        Log.info("Vanilla Biomes: {}", Environment.isVanillaBiomes());
    }

    @Override
    public final void generateStructures(BiomeManager biomes, IChunk chunk, ChunkGenerator<?> generator, TemplateManager templates) {
        structureGenerator.generateStructureStarts(biomes, chunk, generator, templates);
    }

    @Override
    public final void generateStructureStarts(IWorld world, IChunk chunk) {
        structureGenerator.generateStructureReferences(world, chunk);
    }

    @Override
    public final void generateBiomes(IChunk chunk) {
        biomeGenerator.generateBiomes(chunk);
    }

    @Override
    public final void makeBase(IWorld world, IChunk chunk) {
        terrainGenerator.generateTerrain(world, chunk);
    }

    @Override
    public final void func_225551_a_(WorldGenRegion world, IChunk chunk) {
        terrainGenerator.generateSurface(world, chunk);
    }

    @Override
    public final void decorate(WorldGenRegion region) {
        terrainGenerator.generateFeatures(region);
    }

    @Override
    public final void func_225550_a_(BiomeManager biomes, IChunk chunk, GenerationStage.Carving type) {
        terrainCarver.carveTerrain(biomes, chunk, type);
    }

    @Override
    public final void spawnMobs(WorldGenRegion region) {
        mobGenerator.generateMobs(region);
    }

    @Override
    public final void spawnMobs(ServerWorld worldIn, boolean hostile, boolean peaceful) {
        mobGenerator.spawnMobs(worldIn, hostile, peaceful);
    }

    @Override
    public final List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification type, BlockPos pos) {
        return mobGenerator.getPossibleCreatures(world, type, pos);
    }

    public final Biome getBiome(BiomeManager biomes, BlockPos pos) {
        return super.getBiome(biomes, pos);
    }

    @Override
    public final int func_222529_a(int x, int z, Heightmap.Type type) {
        int chunkX = Size.blockToChunk(x);
        int chunkZ = Size.blockToChunk(z);
        ChunkReader chunk = getChunkReader(chunkX, chunkZ);
        Cell cell = chunk.getCell(x, z);
        int level = context.levels.scale(cell.value) + 1;
        if (type == Heightmap.Type.OCEAN_FLOOR || type == Heightmap.Type.OCEAN_FLOOR_WG) {
            return level;
        }
        return Math.max(getSeaLevel(), level);
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    @Override
    public final int getMaxHeight() {
        return getContext().levels.worldHeight;
    }

    @Override
    public final int getSeaLevel() {
        return getContext().levels.waterLevel;
    }

    @Override
    public final int getGroundHeight() {
        return getContext().levels.groundLevel;
    }

    public final TerraContext getContext() {
        return context;
    }

    public final Materials getMaterials() {
        return context.materials;
    }

    public final FeatureManager getFeatureManager() {
        return featureManager;
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

    public final BlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public final List<ColumnDecorator> getBaseDecorators() {
        return baseDecorators;
    }

    public final List<ColumnDecorator> getPostProcessors() {
        return postProcessors;
    }

    public final ChunkReader getChunkReader(int chunkX, int chunkZ) {
        return regionCache.getChunk(chunkX, chunkZ);
    }
}
