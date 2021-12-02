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
import com.terraforged.mod.worldgen.biome.vegetation.BiomeVegetation;
import com.terraforged.mod.worldgen.biome.vegetation.BiomeVegetationManager;
import com.terraforged.mod.worldgen.biome.vegetation.TreePositionSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FeatureDecorator {
    public static final GenerationStep.Decoration[] STAGES = GenerationStep.Decoration.values();
    private static final int MAX_DECORATION_STAGE = GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal();

    private final BiomeVegetationManager vegetation;
    private final Map<GenerationStep.Decoration, List<Supplier<StructureFeature<?>>>> structures;

    public FeatureDecorator(RegistryAccess access) {
        this.vegetation = new BiomeVegetationManager(access);
        this.structures = VanillaDecorator.buildStructureMap(access);
    }

    public BiomeVegetationManager getVegetationManager() {
        return vegetation;
    }

    public List<Supplier<StructureFeature<?>>> getStageStructures(int stage) {
        return structures.get(STAGES[stage]);
    }

    public List<Supplier<PlacedFeature>> getStageFeatures(int stage, Biome biome) {
        var stages = biome.getGenerationSettings().features();
        if (stage >= stages.size()) return Collections.emptyList();
        return stages.get(stage);
    }

    public void decorate(ChunkAccess chunk, WorldGenLevel level, StructureFeatureManager structures, Generator generator) {
        var origin = getOrigin(level, chunk);
        var biome = level.getBiome(origin);
        var random = getRandom();
        long seed = random.setDecorationSeed(level.getSeed(), origin.getX(), origin.getZ());

        decoratePre(seed, origin, biome, chunk, level, generator, random, structures);
        decorateTrees(seed, chunk, level, generator, random);
        decorateOther(seed, origin, biome, level, generator, random);
        decorateGrass(seed, origin, level, generator, random);
        decoratePost(seed, origin, biome, chunk, level, generator, random, structures);
    }

    private void decoratePre(long seed,
                             BlockPos origin,
                             Biome biome,
                             ChunkAccess chunk,
                             WorldGenLevel level,
                             Generator generator,
                             WorldgenRandom random,
                             StructureFeatureManager structureManager) {

        VanillaDecorator.decorate(seed, 0, BiomeVegetation.STAGE - 1, origin, biome, chunk, level, generator, random, structureManager, this);
    }

    private void decoratePost(long seed,
                              BlockPos origin,
                              Biome biome,
                              ChunkAccess chunk,
                              WorldGenLevel level,
                              Generator generator,
                              WorldgenRandom random,
                              StructureFeatureManager structureManager) {

        VanillaDecorator.decorate(seed, BiomeVegetation.STAGE + 1, MAX_DECORATION_STAGE, origin, biome, chunk, level, generator, random, structureManager, this);
    }

    private void decorateTrees(long seed, ChunkAccess chunk, WorldGenLevel level, Generator generator, WorldgenRandom random) {
        TreePositionSampler.place(seed, 0.25F, 0.85F, chunk, level, generator, random, this);
    }

    private void decorateOther(long seed, BlockPos origin, Biome biome, WorldGenLevel level, Generator generator, WorldgenRandom random) {
        var vegetation = getVegetationManager().getVegetation(biome);
        if (vegetation == BiomeVegetation.NONE) return;

        int offset = 245889;
        for (var other : vegetation.getOther()) {
            random.setFeatureSeed(seed, offset++, BiomeVegetation.STAGE);
            other.place(level, generator, random, origin);
        }
    }

    private void decorateGrass(long seed, BlockPos origin, WorldGenLevel level, Generator generator, WorldgenRandom random) {
        var pos = new BlockPos.MutableBlockPos();

        int offset = 9813589;
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                pos.set(origin.getX() + x, origin.getY(), origin.getZ() + z);

                var biome = level.getBiome(pos);
                var vegetation = getVegetationManager().getVegetation(biome);
                if (vegetation == BiomeVegetation.NONE) continue;

                if (random.nextFloat() < 0.1F) {
                    for (var grass : vegetation.getGrass()) {
                        random.setFeatureSeed(seed, offset++, BiomeVegetation.STAGE);
                        grass.place(level, generator, random, pos);
                    }
                }
            }
        }
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
