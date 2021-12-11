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

package com.terraforged.mod.worldgen.cave;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class NoiseCaveDecorator {
    public static void decorate(ChunkAccess chunk, CarverChunk carver, WorldGenLevel region, Generator generator, NoiseCave config) {
        var biomes = carver.getBiomes(config);
        if (biomes == null) return;

        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();
        int startY = config.getHeight(startX, startZ);

        var pos = new BlockPos(startX, startY, startZ);
        var random = new WorldgenRandom(new LegacyRandomSource(region.getSeed()));

        for (int i = 0; i < biomes.size(); i++) {
            var biome = biomes.get(i);
            decorate(pos, region, generator, biome.getGenerationSettings(), random);
        }
    }

    public static void decorate(BlockPos pos,
                                WorldGenLevel region,
                                Generator generator,
                                BiomeGenerationSettings settings,
                                WorldgenRandom random) {

        var features = settings.features();
        long baseSeed = random.setDecorationSeed(region.getSeed(), pos.getX(), pos.getZ());

        for (int stageIndex = 0; stageIndex < features.size(); stageIndex++) {
            if (stageIndex < GenerationStep.Decoration.LOCAL_MODIFICATIONS.ordinal()) continue;

            var stage = features.get(stageIndex);
            for (int featureIndex = 0; featureIndex < stage.size(); featureIndex++) {
                random.setFeatureSeed(baseSeed, featureIndex, stageIndex);

                var feature = stage.get(featureIndex).get();
                feature.placeWithBiomeCheck(region, generator, random, pos);
            }
        }
    }
}
