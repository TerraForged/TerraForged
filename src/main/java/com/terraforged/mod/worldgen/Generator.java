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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.codec.WorldGenCodec;
import com.terraforged.mod.worldgen.biome.BiomeGenerator;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainCache;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.mod.worldgen.util.ChunkUtil;
import com.terraforged.mod.worldgen.util.ThreadPool;
import net.minecraft.core.*;
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
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Generator extends ChunkGenerator implements IGenerator {
    public static final Codec<Generator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("seed", 0L).forGetter(g -> g.seed),
            TerrainLevels.CODEC.optionalFieldOf("levels", TerrainLevels.DEFAULT.get()).forGetter(g -> g.levels),
            WorldGenCodec.CODEC.forGetter(Generator::getRegistries)
    ).apply(instance, instance.stable(GeneratorPreset::build)));

    protected final long seed;
    protected final Source biomeSource;
    protected final TerrainLevels levels;
    protected final VanillaGen vanillaGen;
    protected final BiomeGenerator biomeGenerator;
    protected final INoiseGenerator noiseGenerator;
    protected final TerrainCache terrainCache;
    protected final ThreadLocal<GeneratorResource> localResource = ThreadLocal.withInitial(GeneratorResource::new);

    public Generator(long seed,
                     TerrainLevels levels,
                     VanillaGen vanillaGen,
                     Source biomeSource,
                     BiomeGenerator biomeGenerator,
                     INoiseGenerator noiseGenerator) {
        super(vanillaGen.getStructureSets(), Optional.empty(), biomeSource, biomeSource, seed);
        this.seed = seed;
        this.levels = levels;
        this.vanillaGen = vanillaGen;
        this.biomeSource = biomeSource;
        this.biomeGenerator = biomeGenerator;
        this.noiseGenerator = noiseGenerator;
        this.terrainCache = new TerrainCache(levels, noiseGenerator);
    }

    public long getSeed() {
        return seed;
    }

    protected RegistryAccess getRegistries() {
        return biomeSource.getRegistries();
    }

    public VanillaGen getVanillaGen() {
        return vanillaGen;
    }

    public INoiseGenerator getNoiseGenerator() {
        return noiseGenerator;
    }

    public TerrainData getChunkData(ChunkPos pos) {
        return terrainCache.getNow(pos);
    }

    public CompletableFuture<TerrainData> getChunkDataAsync(ChunkPos pos) {
        return terrainCache.getAsync(pos);
    }

    @Override
    public Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public Generator withSeed(long seed) {
        var noiseGenerator = this.noiseGenerator.with(seed, levels);
        var biomeSource = new Source(seed, noiseGenerator, this.biomeSource);
        var vanillaGen = new VanillaGen(seed, biomeSource, this.vanillaGen);
        var biomeGenerator = new BiomeGenerator(seed, this.biomeGenerator);
        return new Generator(seed, levels, vanillaGen, biomeSource, biomeGenerator, noiseGenerator);
    }

    @Override
    public int getMinY() {
        return levels.minY;
    }

    @Override
    public int getSeaLevel() {
        return levels.seaLevel;
    }

    @Override
    public int getGenDepth() {
        return levels.maxY;
    }

    @Override
    public Source getBiomeSource() {
        return biomeSource;
    }

    @Override
    public Climate.Sampler climateSampler() {
        return Source.NOOP_CLIMATE_SAMPLER;
    }

    @Nullable
    public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestMapFeature(ServerLevel server, HolderSet<ConfiguredStructureFeature<?, ?>> feature, BlockPos pos, int i, boolean first) {
        return super.findNearestMapFeature(server, feature, pos, i, first);
    }

    @Override
    public void createStructures(RegistryAccess access, StructureFeatureManager structureFeatures, ChunkAccess chunk, StructureManager structures, long seed) {
        terrainCache.hint(chunk.getPos());
        super.createStructures(access, structureFeatures, chunk, structures, seed);
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureFeatureManager structureFeatures, ChunkAccess chunk) {
        terrainCache.hint(chunk.getPos());
        super.createReferences(level, structureFeatures, chunk);
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
        biomeGenerator.surface(chunk, region, this);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, BiomeManager biomes, StructureFeatureManager structures, ChunkAccess chunk, GenerationStep.Carving stage) {
        biomeGenerator.carve(seed, chunk, region, biomes, stage, this);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel region, ChunkAccess chunk, StructureFeatureManager structures) {
        biomeGenerator.decorate(chunk, region, structures, this);
        terrainCache.drop(chunk.getPos());
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        // See NoiseBasedChunkGenerator
        var settings = vanillaGen.getSettings().value();
        if (settings.disableMobGeneration()) return;

        var chunkPos = region.getCenter();
        var position = chunkPos.getWorldPosition().atY(region.getMaxBuildHeight() - 1);

        var holder = region.getBiome(position);
        var random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
        random.setDecorationSeed(region.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());

        NaturalSpawner.spawnMobsForChunkGeneration(region, holder, chunkPos, random);
    }

    @Override
    public int getBaseHeight(int x, int z, net.minecraft.world.level.levelgen.Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
        var sample = terrainCache.getSample(x, z);
        float scaledBase = levels.getScaledBaseLevel(sample.baseNoise);
        float scaledHeight = levels.getScaledHeight(sample.heightNoise);
        int base = levels.getHeight(scaledBase);
        int height = levels.getHeight(scaledHeight);

        return switch (types) {
            case WORLD_SURFACE, WORLD_SURFACE_WG, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES -> Math.max(base, height) + 1;
            case OCEAN_FLOOR, OCEAN_FLOOR_WG -> height + 1;
        };
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor) {
        var sample = terrainCache.getSample(x, z);
        float scaledBase = levels.getScaledBaseLevel(sample.baseNoise);
        float scaledHeight = levels.getScaledHeight(sample.heightNoise);

        int base = levels.getHeight(scaledBase);
        int height = levels.getHeight(scaledHeight);
        int surface = Math.max(base, height);

        var states = new BlockState[surface];
        Arrays.fill(states, 0, height, Blocks.STONE.defaultBlockState());
        if (surface > height) {
            Arrays.fill(states, height, surface, Blocks.WATER.defaultBlockState());
        }

        return new NoiseColumn(height, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, BlockPos pos) {
        var terrainSample = terrainCache.getSample(pos.getX(), pos.getZ());
        var climateSample = biomeSource.getBiomeSampler().sample(pos.getX(), pos.getZ());

        var terrainType = terrainSample.terrainType;
        var climateType = climateSample.cell.biome;

        lines.add("");
        lines.add("[TerraForged]");
        lines.add("Terrain Type: " + terrainType.getName());
        lines.add("Climate Type: " + climateType.name());
        lines.add("Continent Edge: " + climateSample.continentNoise);
        lines.add("Base Level: " + terrainSample.baseNoise);
        lines.add("River Proximity: " + (1 - climateSample.riverNoise));
    }
}
