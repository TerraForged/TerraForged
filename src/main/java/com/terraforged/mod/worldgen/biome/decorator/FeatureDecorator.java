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

package com.terraforged.mod.worldgen.biome.decorator;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.biome.vegetation.BiomeVegetationManager;
import com.terraforged.mod.worldgen.biome.vegetation.VegetationFeatures;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import net.minecraft.core.*;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FeatureDecorator {
    public static final GenerationStep.Decoration[] STAGES = GenerationStep.Decoration.values();
    private static final int MAX_DECORATION_STAGE = GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal();

    private final BiomeVegetationManager vegetation;
    private final Map<GenerationStep.Decoration, List<Holder<ConfiguredStructureFeature<?, ?>>>> structures;

    public FeatureDecorator(RegistryAccess access) {
        this.vegetation = new BiomeVegetationManager(access);
        this.structures = VanillaDecorator.buildStructureMap(access);
    }

    public BiomeVegetationManager getVegetationManager() {
        return vegetation;
    }

    public List<Holder<ConfiguredStructureFeature<?, ?>>> getStageStructures(int stage) {
        return structures.get(STAGES[stage]);
    }

    public HolderSet<PlacedFeature> getStageFeatures(int stage, Biome biome) {
        var stages = biome.getGenerationSettings().features();
        if (stage >= stages.size()) return null;
        return stages.get(stage);
    }

    public void decorate(ChunkAccess chunk,
                         WorldGenLevel level,
                         StructureFeatureManager structures,
                         CompletableFuture<TerrainData> terrain,
                         Generator generator) {
        var origin = getOrigin(level, chunk);
        var biome = level.getBiome(origin);
        var random = getRandom();
        long seed = random.setDecorationSeed(level.getSeed(), origin.getX(), origin.getZ());

        decoratePre(seed, origin, biome, chunk, level, generator, random, structures);
        decorateVegetation(seed, origin, biome, chunk, level, generator, random, terrain);
        decoratePost(seed, origin, biome, chunk, level, generator, random, structures);
    }

    private void decoratePre(long seed,
                             BlockPos origin,
                             Holder<Biome> biome,
                             ChunkAccess chunk,
                             WorldGenLevel level,
                             Generator generator,
                             WorldgenRandom random,
                             StructureFeatureManager structureManager) {

        VanillaDecorator.decorate(seed, 0, VegetationFeatures.STAGE - 1, origin, biome, chunk, level, generator, random, structureManager, this);
    }

    private void decoratePost(long seed,
                              BlockPos origin,
                              Holder<Biome> biome,
                              ChunkAccess chunk,
                              WorldGenLevel level,
                              Generator generator,
                              WorldgenRandom random,
                              StructureFeatureManager structureManager) {

        VanillaDecorator.decorate(seed, VegetationFeatures.STAGE + 1, MAX_DECORATION_STAGE, origin, biome, chunk, level, generator, random, structureManager, this);
    }

    private void decorateVegetation(long seed,
                                    BlockPos origin,
                                    Holder<Biome> biome,
                                    ChunkAccess chunk,
                                    WorldGenLevel level,
                                    Generator generator,
                                    WorldgenRandom random,
                                    CompletableFuture<TerrainData> terrain) {

        PositionSampler.placeVegetation(seed, origin, biome, chunk, level, generator, random, terrain, this);
    }

    private static BlockPos getOrigin(WorldGenLevel level, ChunkAccess chunk) {
        var chunkPos = chunk.getPos();
        var sectionPos = SectionPos.of(chunkPos, level.getMinSection());
        return sectionPos.origin();
    }

    private static WorldgenRandom getRandom() {
        return new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
    }
}
