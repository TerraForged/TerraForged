package com.terraforged.mod.worldgen.util;

import com.mojang.serialization.Codec;
import com.terraforged.mod.worldgen.Generator;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class DelegateGenerator extends ChunkGenerator {
    private final Generator generator;
    private final StructureSettings settings;

    public DelegateGenerator(long seed, Generator generator, StructureSettings settings) {
        super(generator.getBiomeSource(), generator.getBiomeSource(), settings, seed);
        this.generator = generator;
        this.settings = settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChunkGenerator withSeed(long l) {
        return this;
    }

    @Override
    public StructureSettings getSettings() {
        return settings;
    }

    @Override
    public Climate.Sampler climateSampler() {
        return generator.climateSampler();
    }

    @Override
    public int getGenDepth() {
        return generator.getGenDepth();
    }

    @Override
    public int getSeaLevel() {
        return generator.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return generator.getMinY();
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
        return generator.getBaseHeight(x, z, types, levelHeightAccessor);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor) {
        return generator.getBaseColumn(x, z, levelHeightAccessor);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        return generator.fillFromNoise(executor, blender, structureFeatureManager, chunkAccess);
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, BiomeManager biomeManager, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
        generator.applyCarvers(worldGenRegion, l, biomeManager, structureFeatureManager, chunkAccess, carving);
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        generator.buildSurface(worldGenRegion, structureFeatureManager, chunkAccess);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        generator.spawnOriginalMobs(worldGenRegion);
    }
}
