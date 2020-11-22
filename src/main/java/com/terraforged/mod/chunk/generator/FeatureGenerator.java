/*
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

package com.terraforged.mod.chunk.generator;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.fm.biome.BiomeFeature;
import com.terraforged.fm.biome.BiomeFeatures;
import com.terraforged.fm.util.identity.Identity;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.fix.RegionFix;
import com.terraforged.mod.chunk.util.DecoratorException;
import com.terraforged.mod.chunk.util.TerraContainer;
import com.terraforged.mod.util.Environment;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;

import java.util.List;

public class FeatureGenerator implements Generator.Features {

    private static final int FEATURE_STAGES = GenerationStage.Decoration.values().length;
    private static final String STRUCTURE = "structure";
    private static final String FEATURE = "feature";
    private static final long WARN_TIME = 100;

    private final TFChunkGenerator generator;

    public FeatureGenerator(TFChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public final void generateFeatures(WorldGenRegion region, StructureManager manager) {
        int chunkX = region.getMainChunkX();
        int chunkZ = region.getMainChunkZ();
        IChunk chunk = region.getChunk(chunkX, chunkZ);

        ChunkReader reader = generator.getChunkReader(chunkX, chunkZ);
        TerraContainer container = TerraContainer.getOrCreate(chunk, reader, generator.getBiomeProvider());

        // de-hardcode sea-level
        RegionFix regionFix = new RegionFix(region, generator);

        Biome biome = container.getFeatureBiome();
        try (DecoratorContext context = generator.getContext().decorator(chunk)) {
            BlockPos pos = new BlockPos(context.blockX, 0, context.blockZ);

            // place biome features
            decorate(manager, regionFix, chunk, biome, pos);

            // run post processes on chunk
            postProcess(reader, container, context);

            // bake biome array
            ((ChunkPrimer) chunk).setBiomes(container.bakeBiomes(Environment.isVanillaBiomes(), generator.getContext().gameContext));

            // close the current chunk reader
            reader.close();

            // mark chunk disposed as this is the last usage of the reader
            reader.dispose();
        } catch (DecoratorException e) {
            // TODO: crash
            e.printStackTrace();
        }
    }

    private void decorate(StructureManager manager, ISeedReader region, IChunk chunk, Biome biome, BlockPos pos) throws DecoratorException {
        SharedSeedRandom random = new SharedSeedRandom();
        long decorationSeed = random.setDecorationSeed(region.getSeed(), pos.getX(), pos.getZ());

        BiomeFeatures biomeFeatures = generator.getFeatureManager().getFeatures(biome);
        List<List<BiomeFeature>> stagedFeatures = biomeFeatures.getFeatures();
        List<List<Structure<?>>> stagedStructures = biomeFeatures.getStructures();

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        int startX = chunkPos.getXStart();
        int startZ = chunkPos.getZStart();
        MutableBoundingBox chunkBounds = new MutableBoundingBox(startX, startZ, startX + 15, startZ + 15);

        long timeStamp = 0L;
        for (int stageIndex = 0; stageIndex < FEATURE_STAGES; stageIndex++) {
            int featureSeed = 0;

            if (stageIndex < stagedStructures.size()) {
                List<Structure<?>> structures = stagedStructures.get(stageIndex);
                for (int structureIndex = 0; structureIndex < structures.size(); structureIndex++) {
                    Structure<?> structure = structures.get(structureIndex);
                    random.setFeatureSeed(decorationSeed, featureSeed++, stageIndex);
                    try {
                        timeStamp = System.currentTimeMillis();
                        manager.func_235011_a_(SectionPos.from(pos), structure).forEach(start -> start.func_230366_a_(
                                region,
                                manager,
                                generator,
                                random,
                                chunkBounds,
                                chunkPos
                        ));
                        checkTime(STRUCTURE, structure.getStructureName(), timeStamp);
                    } catch (Throwable t) {
                        throw new DecoratorException(STRUCTURE, structure.getStructureName(), t);
                    }
                }
            }

            if (stageIndex < stagedFeatures.size()) {
                List<BiomeFeature> features = stagedFeatures.get(stageIndex);
                for (int featureIndex = 0; featureIndex < features.size(); featureIndex++) {
                    BiomeFeature feature = features.get(featureIndex);
                    random.setFeatureSeed(decorationSeed, featureSeed++, stageIndex);
                    if (feature.getPredicate().test(chunk, biome)) {
                        try {
                            timeStamp = System.currentTimeMillis();
                            feature.getFeature().generate(region, generator, random, pos);
                            checkTime(FEATURE, feature.getIdentity(), timeStamp);
                        } catch (Throwable t) {
                            throw new DecoratorException(FEATURE, feature.getIdentity().getIdentity(), t);
                        }
                    }
                }
            }
        }
    }

    private void postProcess(ChunkReader reader, TerraContainer container, DecoratorContext context) {
        List<ColumnDecorator> decorators = generator.getPostProcessors();
        reader.iterate(context, (cell, dx, dz, ctx) -> {
            int px = ctx.blockX + dx;
            int pz = ctx.blockZ + dz;
            int py = ctx.chunk.getTopBlockY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, dx, dz);
            ctx.cell = cell;
            ctx.biome = container.getBiome(dx, dz);
            for (ColumnDecorator decorator : decorators) {
                decorator.decorate(ctx.chunk, ctx, px, py, pz);
            }
        });
    }

    private static void checkTime(String type, String identity, long timestamp) {
        long duration = System.currentTimeMillis() - timestamp;
        if (duration > WARN_TIME) {
            Log.err("{} took {}ms to generate!. Identity: {}", type, duration, identity);
        }
    }

    private static void checkTime(String type, Identity identity, long timestamp) {
        long duration = System.currentTimeMillis() - timestamp;
        if (duration > WARN_TIME) {
            Log.err("{} took {}ms to generate!. Identity: {}", type, duration, identity.getIdentity());
        }
    }
}
