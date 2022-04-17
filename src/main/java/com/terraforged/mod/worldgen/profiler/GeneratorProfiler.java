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

package com.terraforged.mod.worldgen.profiler;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.terraforged.mod.util.ReflectionUtil;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class GeneratorProfiler extends ChunkGenerator {
    public static final Codec<GeneratorProfiler> CODEC = ChunkGenerator.CODEC.xmap(GeneratorProfiler::wrap, GeneratorProfiler::getGenerator);
    public static final AtomicBoolean PROFILING = new AtomicBoolean(true);

    protected static final MethodHandle STRUCTURE_REGISTRY = ReflectionUtil.field(ChunkGenerator.class, Registry.class);
    protected static final MethodHandle STRUCTURE_OVERRIDES = ReflectionUtil.field(ChunkGenerator.class, Optional.class);

    protected final ChunkGenerator generator;
    protected final ProfilerStages stages = new ProfilerStages();

    private GeneratorProfiler(Registry<StructureSet> structures, Optional<HolderSet<StructureSet>> overrides, ChunkGenerator generator) {
        super(structures, overrides, generator.getBiomeSource());
        this.generator = generator;
    }

    public ChunkGenerator getGenerator() {
        return generator;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public Optional<ResourceKey<Codec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
        return generator.getTypeNameForDataFixer();
    }

    @Override
    public ChunkGenerator withSeed(long p_62156_) {
        return generator.withSeed(p_62156_);
    }

    @Override
    public Climate.Sampler climateSampler() {
        return generator.climateSampler();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int p_187755_, int p_187756_, int p_187757_) {
        return generator.getNoiseBiome(p_187755_, p_187756_, p_187757_);
    }

    @Override
    public void createStructures(RegistryAccess p_62200_, StructureFeatureManager p_62201_, ChunkAccess p_62202_, StructureManager p_62203_, long p_62204_) {
        var timer = stages.starts.start();
        generator.createStructures(p_62200_, p_62201_, p_62202_, p_62203_, p_62204_);
        timer.punchOut();

        stages.tick();
    }

    @Override
    public void createReferences(WorldGenLevel p_62178_, StructureFeatureManager p_62179_, ChunkAccess p_62180_) {
        var timer = stages.refs.start();
        generator.createReferences(p_62178_, p_62179_, p_62180_);
        timer.punchOut();
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> p_196743_, Executor p_196744_, Blender p_196745_, StructureFeatureManager p_196746_, ChunkAccess p_196747_) {
        return CompletableFuture.completedFuture(stages.biomes.start()).thenCombine(
                generator.createBiomes(p_196743_, p_196744_, p_196745_, p_196746_, p_196747_),
                GenTimer::punchOut
        );
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_187748_, Blender p_187749_, StructureFeatureManager p_187750_, ChunkAccess p_187751_) {
        return CompletableFuture.completedFuture(stages.noise.start()).thenCombine(
                generator.fillFromNoise(p_187748_, p_187749_, p_187750_, p_187751_),
                GenTimer::punchOut
        );
    }


    @Override
    public void buildSurface(WorldGenRegion p_187697_, StructureFeatureManager p_187698_, ChunkAccess p_187699_) {
        var timer = stages.surface.start();
        generator.buildSurface(p_187697_, p_187698_, p_187699_);
        timer.punchOut();
    }

    @Override
    public void applyCarvers(WorldGenRegion p_187691_, long p_187692_, BiomeManager p_187693_, StructureFeatureManager p_187694_, ChunkAccess p_187695_, GenerationStep.Carving p_187696_) {
        var timer = stages.carve.start();
        generator.applyCarvers(p_187691_, p_187692_, p_187693_, p_187694_, p_187695_, p_187696_);
        timer.punchOut();
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel p_187712_, ChunkAccess p_187713_, StructureFeatureManager p_187714_) {
        var timer = stages.decoration.start();
        generator.applyBiomeDecoration(p_187712_, p_187713_, p_187714_);
        timer.punchOut();
        stages.incrementChunks();
    }

    @Override
    public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestMapFeature(ServerLevel p_62162_, HolderSet<ConfiguredStructureFeature<?, ?>> p_62163_, BlockPos p_62164_, int p_62165_, boolean p_62166_) {
        return generator.findNearestMapFeature(p_62162_, p_62163_, p_62164_, p_62165_, p_62166_);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion p_62167_) {
        generator.spawnOriginalMobs(p_62167_);
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor p_156157_) {
        return generator.getSpawnHeight(p_156157_);
    }

    @Override
    public BiomeSource getBiomeSource() {
        return generator.getBiomeSource();
    }

    @Override
    public int getGenDepth() {
        return generator.getGenDepth();
    }

/*    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome p_156158_, StructureFeatureManager p_156159_, MobCategory p_156160_, BlockPos p_156161_) {
        return generator.getMobsAt(p_156158_, p_156159_, p_156160_, p_156161_);
    }

    @Override
    public boolean validBiome(Registry<Biome> p_187736_, Predicate<ResourceKey<Biome>> p_187737_, Biome p_187738_) {
        return p_187736_.getResourceKey(p_187738_).filter(p_187737_).isPresent();
    }*/

    @Override
    public int getSeaLevel() {
        return generator.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return generator.getMinY();
    }

    @Override
    public int getBaseHeight(int p_156153_, int p_156154_, Heightmap.Types p_156155_, LevelHeightAccessor p_156156_) {
        return generator.getBaseHeight(p_156153_, p_156154_, p_156155_, p_156156_);
    }

    @Override
    public NoiseColumn getBaseColumn(int p_156150_, int p_156151_, LevelHeightAccessor p_156152_) {
        return generator.getBaseColumn(p_156150_, p_156151_, p_156152_);
    }

    @Override
    public int getFirstFreeHeight(int p_156175_, int p_156176_, Heightmap.Types p_156177_, LevelHeightAccessor p_156178_) {
        return generator.getFirstFreeHeight(p_156175_, p_156176_, p_156177_, p_156178_);
    }

    @Override
    public int getFirstOccupiedHeight(int p_156180_, int p_156181_, Heightmap.Types p_156182_, LevelHeightAccessor p_156183_) {
        return generator.getFirstOccupiedHeight(p_156180_, p_156181_, p_156182_, p_156183_);
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, BlockPos pos) {
        generator.addDebugScreenInfo(lines, pos);
    }

    @SuppressWarnings("unchecked")
    private static Registry<StructureSet> getStructures(ChunkGenerator generator) {
        try {
            return (Registry<StructureSet>) STRUCTURE_REGISTRY.invokeExact(generator);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    @SuppressWarnings({"unchecked"})
    private static Optional<HolderSet<StructureSet>> getOverrides(ChunkGenerator generator) {
        try {
            return (Optional<HolderSet<StructureSet>>) STRUCTURE_OVERRIDES.invokeExact(generator);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    public static GeneratorProfiler wrap(ChunkGenerator generator) {
        if (generator instanceof GeneratorProfiler profiler) {
            generator = profiler.getGenerator();
        }

        var structures = getStructures(generator);
        var overrides = getOverrides(generator);

        return new GeneratorProfiler(structures, overrides, generator);
    }
}
