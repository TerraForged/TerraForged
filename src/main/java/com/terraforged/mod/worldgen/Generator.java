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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.data.codec.WorldGenCodec;
import com.terraforged.mod.worldgen.biome.BiomeGenerator;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainCache;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.mod.worldgen.util.ChunkUtil;
import com.terraforged.mod.worldgen.util.ThreadPool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Generator extends ChunkGenerator implements IGenerator {
    public static final Codec<Generator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TerrainLevels.CODEC.optionalFieldOf("levels", TerrainLevels.DEFAULT.get()).forGetter(g -> g.levels),
            WorldGenCodec.CODEC.forGetter(Generator::getRegistries)
    ).apply(instance, instance.stable(GeneratorPreset::build)));

    protected final Source biomeSource;
    protected final TerrainLevels levels;
    protected final VanillaGen vanillaGen;
    protected final BiomeGenerator biomeGenerator;
    protected final INoiseGenerator noiseGenerator;
    protected final TerrainCache terrainCache;
    protected final ThreadLocal<GeneratorResource> localResource = ThreadLocal.withInitial(GeneratorResource::new);

    public Generator(TerrainLevels levels,
                     VanillaGen vanillaGen,
                     Source biomeSource,
                     BiomeGenerator biomeGenerator,
                     INoiseGenerator noiseGenerator) {
        super(vanillaGen.getStructureSets(), Optional.empty(), biomeSource);
        this.levels = levels;
        this.vanillaGen = vanillaGen;
        this.biomeSource = biomeSource;
        this.biomeGenerator = biomeGenerator;
        this.noiseGenerator = noiseGenerator;
        this.terrainCache = new TerrainCache(levels, noiseGenerator);
    }

    @Override
    public void ensureStructuresGenerated(RandomState state) {
        biomeSource.withSeed(state.legacyLevelSeed());

        super.ensureStructuresGenerated(state);
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

    public TerrainData getChunkData(int seed, ChunkPos pos) {
        return terrainCache.getNow(seed, pos);
    }

    public CompletableFuture<TerrainData> getChunkDataAsync(int seed, ChunkPos pos) {
        return terrainCache.getAsync(seed, pos);
    }

    @Override
    public Codec<? extends ChunkGenerator> codec() {
        return CODEC;
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
    public void createStructures(RegistryAccess access, RandomState state, StructureManager structures, ChunkAccess chunk, StructureTemplateManager templates, long seed) {
        terrainCache.hint(Seeds.get(state), chunk.getPos());
        super.createStructures(access, state, structures, chunk, templates, seed);
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureManager structureFeatures, ChunkAccess chunk) {
        terrainCache.hint(Seeds.get(level.getSeed()), chunk.getPos());
        super.createReferences(level, structureFeatures, chunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> registry, Executor executor, RandomState state, Blender blender, StructureManager structures, ChunkAccess chunk) {
        terrainCache.hint(Seeds.get(state), chunk.getPos());
        return CompletableFuture.supplyAsync(() -> {
            ChunkUtil.fillNoiseBiomes(chunk, biomeSource, localResource.get());
            return chunk;
        }, ThreadPool.EXECUTOR);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState state, StructureManager structureManager, ChunkAccess chunkAccess) {
        return terrainCache.combineAsync(executor, Seeds.get(state), chunkAccess, (chunk, terrainData) -> {
            ChunkUtil.fillChunk(getSeaLevel(), chunk, terrainData, ChunkUtil.FILLER, localResource.get());
            ChunkUtil.primeHeightmaps(getSeaLevel(), chunk, terrainData, ChunkUtil.FILLER);
            ChunkUtil.buildStructureTerrain(chunk, terrainData, structureManager);
            return chunk;
        });
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState state, ChunkAccess chunk) {
        biomeGenerator.surface(chunk, region, state, this);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState state, BiomeManager biomes, StructureManager structures, ChunkAccess chunk, GenerationStep.Carving stage) {
        biomeGenerator.carve(seed, chunk, region, biomes, stage, this);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel region, ChunkAccess chunk, StructureManager structures) {
        int seed = Seeds.get(region.getSeed());
        biomeGenerator.decorate(chunk, region, structures, this);
        terrainCache.drop(seed, chunk.getPos());
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        // See NoiseBasedChunkGenerator
        var settings = vanillaGen.getSettings().value();
        if (settings.disableMobGeneration()) return;

        var chunkPos = region.getCenter();
        var position = chunkPos.getWorldPosition().atY(region.getMaxBuildHeight() - 1);

        var holder = region.getBiome(position);
        var random = new WorldgenRandom(new LegacyRandomSource(region.getSeed()));
        random.setDecorationSeed(region.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());

        NaturalSpawner.spawnMobsForChunkGeneration(region, holder, chunkPos, random);
    }

    @Override
    public int getBaseHeight(int x, int z, net.minecraft.world.level.levelgen.Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState state) {
        var sample = terrainCache.getSample(Seeds.get(state), x, z);

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
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor, RandomState state) {
        var sample = terrainCache.getSample(Seeds.get(state), x, z);

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
    public void addDebugScreenInfo(List<String> lines, RandomState state, BlockPos pos) {
        int seed = Seeds.get(state.legacyLevelSeed());

        var sample = biomeSource.getBiomeSampler().getSample();
        terrainCache.sample(seed, pos.getX(), pos.getZ(), sample);
        biomeSource.getBiomeSampler().sample(seed, pos.getX(), pos.getZ(), sample);

        lines.add("");
        lines.add("[TerraForged]");
        lines.add("Terrain Type: " + sample.terrainType.getName());
        lines.add("Climate Type: " + sample.climateType.name());
        lines.add("Base Noise: " + sample.baseNoise);
        lines.add("Height Noise: " + sample.heightNoise);
        lines.add("Ocean Proximity: " + (1 - sample.continentNoise));
        lines.add("River Proximity: " + (1 - sample.riverNoise));
    }

    public static boolean isTerraForged(ChunkGenerator generator) {
        return generator instanceof Generator || true; // TODO: remove || true
    }
}
