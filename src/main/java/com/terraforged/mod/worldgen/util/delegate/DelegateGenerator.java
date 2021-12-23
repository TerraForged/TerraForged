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

package com.terraforged.mod.worldgen.util.delegate;

import com.mojang.serialization.Codec;
import com.terraforged.mod.worldgen.util.StructureReporter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public abstract class DelegateGenerator extends ChunkGenerator {
    private final ChunkGenerator generator;
    private final StructureReporter reporter;

    public DelegateGenerator(long seed, ChunkGenerator generator, Supplier<NoiseGeneratorSettings> settings) {
        super(generator.getBiomeSource(), generator.getBiomeSource(), settings.get().structureSettings(), seed);
        this.generator = generator;
        this.reporter = new StructureReporter(settings);
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
    public void createStructures(RegistryAccess access, StructureFeatureManager structureFeatures, ChunkAccess chunk, StructureManager structures, long seed) {
        reporter.init();
        super.createStructures(access, structureFeatures, chunk, structures, seed);
    }

    @Override
    public void createReferences(WorldGenLevel level, StructureFeatureManager structureFeatures, ChunkAccess chunk) {
        super.createReferences(level, structureFeatures, chunk);
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
