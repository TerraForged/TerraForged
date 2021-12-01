package com.terraforged.mod.worldgen;

import com.mojang.serialization.Codec;
import com.terraforged.mod.util.ChunkUtil;
import com.terraforged.mod.worldgen.biome.BiomeGenerator;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.feature.StructureGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.mod.worldgen.terrain.TerrainGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
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
    protected final ChunkGenerator vanilla;
    protected final Source biomeSource;
    protected final Climate.Sampler climateSampler;
    protected final BiomeGenerator biomeGenerator;
    protected final TerrainGenerator terrainGenerator;
    protected final StructureGenerator structureGenerator;
    protected final GeneratorCache terrainCache;

    public Generator(long seed,
                     ChunkGenerator vanilla,
                     Source biomeSource,
                     BiomeGenerator biomeGenerator,
                     TerrainGenerator terrainGenerator,
                     StructureGenerator structureGenerator) {
        super(biomeSource, biomeSource, structureGenerator.getSettings(), seed);
        this.seed = seed;
        this.vanilla = vanilla;
        this.biomeSource = biomeSource;
        this.climateSampler = biomeSource.createSampler(seed, this);
        this.terrainGenerator = terrainGenerator.withGenerator(seed, this);
        this.biomeGenerator = biomeGenerator.withGenerator(seed, this);
        this.structureGenerator = structureGenerator.withGenerator(seed, this);
        this.terrainCache = new GeneratorCache(terrainGenerator);
    }

    public TerrainData getChunkData(ChunkPos pos) {
        return terrainCache.getNow(pos);
    }

    @Override
    public Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        if (seed == this.seed) return this;

        return new Generator(seed, vanilla.withSeed(seed), biomeSource.withSeed(seed), biomeGenerator, terrainGenerator, structureGenerator);
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getGenDepth() {
        return 384;
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
        return climateSampler;
    }

    @Override
    public boolean hasStronghold(ChunkPos pos) {
        return structureGenerator.hasStronghold(pos);
    }

    @Nullable
    public BlockPos findNearestMapFeature(ServerLevel server, StructureFeature<?> feature, BlockPos pos, int i, boolean first) {
        return structureGenerator.find(server, feature, pos, i, first);
    }

    @Override
    public void createStructures(RegistryAccess access, StructureFeatureManager structureFeatures, ChunkAccess chunk, StructureManager structures, long seed) {
        terrainCache.hint(chunk.getPos());

        try (var timer = Stage.STRUCTURE_STARTS.start()) {
            structureGenerator.generateStarts(seed, chunk, structureFeatures, structures, access);
        }
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureFeatureManager structureFeatures, ChunkAccess chunk) {
        terrainCache.hint(chunk.getPos());

        try (var timer = Stage.STRUCTURE_REFS.start()) {
            structureGenerator.generateRefs(chunk, level, structureFeatures);
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureManager, ChunkAccess chunkAccess) {
        return terrainCache.combineAsync(executor, chunkAccess, (chunk, terrainData) -> {
            ChunkUtil.fillChunk(getSeaLevel(), chunk, terrainData, Generator::getFiller);
            ChunkUtil.primeHeightmaps(getSeaLevel(), chunk, terrainData, Generator::getFiller);
            ChunkUtil.buildStructureTerrain(chunk, terrainData, structureManager);
            return chunk;
        });
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureFeatureManager structures, ChunkAccess chunk) {
        try (var timer = Stage.SURFACE.start()) {
            biomeGenerator.getSurfaceDecorator().decorate(chunk, this);
//            vanilla.buildSurface(region, structures, chunk);
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, BiomeManager biomes, StructureFeatureManager structures, ChunkAccess chunk, GenerationStep.Carving stage) {
        try (var timer = Stage.CARVER.start()) {
//            vanilla.applyCarvers(region, seed, biomes, structures, chunk, stage);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureFeatureManager structures) {
        try (var timer = Stage.DECORATION.start()) {
            biomeGenerator.getFeatureDecorator().decorate(chunk, level, structures, this);
            terrainCache.drop(chunk.getPos());
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
//        vanilla.spawnOriginalMobs(region);
    }

    @Override
    public int getBaseHeight(int x, int z, net.minecraft.world.level.levelgen.Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
        int height = terrainGenerator.getHeight(x, z) + 1;
        return switch (types) {
            case WORLD_SURFACE, WORLD_SURFACE_WG, MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES -> Math.max(getSeaLevel(), height);
            case OCEAN_FLOOR, OCEAN_FLOOR_WG -> height;
        };
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor) {
        int height = terrainGenerator.getHeight(x, z) + 1;
        int surface = Math.max(getSeaLevel(), height);

        var states = new BlockState[surface];
        Arrays.fill(states, 0, height, Blocks.STONE.defaultBlockState());
        if (surface > height) {
            Arrays.fill(states, height, surface, Blocks.WATER.defaultBlockState());
        }

        return new NoiseColumn(height, states);
    }

    protected static BlockState getFiller(int y, int height) {
        return y >= height ? Blocks.WATER.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }
}
