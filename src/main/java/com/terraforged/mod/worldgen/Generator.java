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

package com.terraforged.mod.worldgen;

import com.mojang.serialization.Codec;
import com.terraforged.mod.worldgen.biome.BiomeComponents;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.biome.surface.SurfaceRegion;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainCache;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.mod.worldgen.util.*;
import com.terraforged.mod.worldgen.util.delegate.DelegateGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Generator extends ChunkGenerator {
    public static final Codec<Generator> CODEC = new GeneratorCodec().stable();

    protected final long seed;
    protected final Source biomeSource;
    protected final TerrainLevels levels;
    protected final VanillaGen vanillaGen;
    protected final StructureConfig structureConfig;
    protected final BiomeComponents biomeGenerator;
    protected final INoiseGenerator noiseGenerator;
    protected final TerrainCache terrainCache;
    protected final ChunkGenerator structureGenerator;
    protected final ThreadLocal<GeneratorResource> localResource = ThreadLocal.withInitial(GeneratorResource::new);

    public Generator(long seed,
                     TerrainLevels levels,
                     VanillaGen vanillaGen,
                     Source biomeSource,
                     BiomeComponents biomeGenerator,
                     INoiseGenerator noiseGenerator,
                     StructureConfig structureConfig) {
        super(biomeSource, biomeSource, structureConfig.copy(), seed);
        this.seed = seed;
        this.levels = levels;
        this.vanillaGen = vanillaGen;
        this.biomeSource = biomeSource;
        this.structureConfig = structureConfig;
        this.biomeGenerator = biomeGenerator;
        this.noiseGenerator = noiseGenerator;
        this.terrainCache = new TerrainCache(levels, noiseGenerator);
        this.structureGenerator = new DelegateGenerator(seed, this, structureConfig) {};
    }

    public TerrainData getChunkData(ChunkPos pos) {
        return terrainCache.getNow(pos);
    }

    public CompletableFuture<TerrainData> getChunkDataAsync(ChunkPos pos) {
        return terrainCache.getAsync(pos);
    }

    public VanillaGen getVanillaGen() {
        return vanillaGen;
    }

    @Override
    public Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        var noiseGenerator = this.noiseGenerator.with(seed, levels);
        var biomeSource = new Source(seed, noiseGenerator, this.biomeSource);
        var vanillaGen = new VanillaGen(seed, biomeSource, this.vanillaGen);
        return new Generator(seed, levels, vanillaGen, biomeSource, biomeGenerator, noiseGenerator, structureConfig);
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getSeaLevel() {
        return levels.seaLevel;
    }

    @Override
    public int getGenDepth() {
        return levels.genDepth;
    }

    @Override
    public Source getBiomeSource() {
        return biomeSource;
    }

    @Override
    public StructureSettings getSettings() {
        return structureGenerator.getSettings();
    }

    @Override
    public Climate.Sampler climateSampler() {
        return Source.NoopSampler.INSTANCE;
    }

    @Override
    public boolean hasStronghold(ChunkPos pos) {
        return structureGenerator.hasStronghold(pos);
    }

    @Nullable
    public BlockPos findNearestMapFeature(ServerLevel server, StructureFeature<?> feature, BlockPos pos, int i, boolean first) {
        return structureGenerator.findNearestMapFeature(server, feature, pos, i, first);
    }

    @Override
    public void createStructures(RegistryAccess access, StructureFeatureManager structureFeatures, ChunkAccess chunk, StructureManager structures, long seed) {
        terrainCache.hint(chunk.getPos());
        structureGenerator.createStructures(access, structureFeatures, chunk, structures, seed);
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureFeatureManager structureFeatures, ChunkAccess chunk) {
        terrainCache.hint(chunk.getPos());
        structureGenerator.createReferences(level, structureFeatures, chunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> registry, Executor executor, Blender blender, StructureFeatureManager structures, ChunkAccess chunk) {
        terrainCache.hint(chunk.getPos());
        return CompletableFuture.supplyAsync(() -> {
            ChunkUtil.fillNoiseBiomes(chunk, biomeSource, climateSampler(), localResource.get());
            return chunk;
        }, ThreadPool.EXECUTOR);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureManager, ChunkAccess chunkAccess) {
        return terrainCache.combineAsync(executor, chunkAccess, (chunk, terrainData) -> {
            ChunkUtil.fillChunk(getSeaLevel(), chunk, terrainData, ChunkUtil.FILLER, localResource.get());
            ChunkUtil.primeHeightmaps(getSeaLevel(), chunk, terrainData, ChunkUtil.FILLER);
            ChunkUtil.buildStructureTerrain(chunk, terrainData, structureManager);
            return chunk;
        });
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureFeatureManager structures, ChunkAccess chunk) {
        NoiseChunkUtil.initChunk(chunk, this);
        region = SurfaceRegion.wrap(region);

        vanillaGen.getVanillaGenerator().buildSurface(region, structures, chunk);
        biomeGenerator.getSurfaceDecorator().decorate(chunk, this);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, BiomeManager biomes, StructureFeatureManager structures, ChunkAccess chunk, GenerationStep.Carving stage) {
        CarverUtil.applyCarvers(seed, chunk, region, biomes, stage, this);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureFeatureManager structures) {
        biomeGenerator.getFeatureDecorator().decorate(chunk, level, structures, this);
        terrainCache.drop(chunk.getPos());
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        vanillaGen.getVanillaGenerator().spawnOriginalMobs(region);
    }

    @Override
    public int getBaseHeight(int x, int z, net.minecraft.world.level.levelgen.Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
        int height = terrainCache.getHeight(x, z) + 1;
        return switch (types) {
            case WORLD_SURFACE, WORLD_SURFACE_WG, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES -> Math.max(getSeaLevel(), height);
            case OCEAN_FLOOR, OCEAN_FLOOR_WG -> height;
        };
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor) {
        int height = terrainCache.getHeight(x, z) + 1;
        int surface = Math.max(getSeaLevel(), height);

        var states = new BlockState[surface];
        Arrays.fill(states, 0, height, Blocks.STONE.defaultBlockState());
        if (surface > height) {
            Arrays.fill(states, height, surface, Blocks.WATER.defaultBlockState());
        }

        return new NoiseColumn(height, states);
    }
}
