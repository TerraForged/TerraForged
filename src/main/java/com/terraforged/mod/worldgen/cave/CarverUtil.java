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
import com.terraforged.mod.worldgen.util.NoiseChunkUtil;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;

import java.util.List;

public class CarverUtil {
    public static void applyCarvers(long seed,
                             ChunkAccess centerChunk,
                             WorldGenRegion region,
                             BiomeManager biomeManager,
                             GenerationStep.Carving step,
                             Generator generator) {

        biomeManager = biomeManager.withDifferentSource(generator);

        var centerPos = centerChunk.getPos();
        var random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));

        var noiseChunk = NoiseChunkUtil.getNoiseChunk(centerChunk, generator);
        var context = generator.getVanillaGen().createCarvingContext(region, centerChunk, noiseChunk);

        var aquifer = noiseChunk.aquifer();
        var mask = ((ProtoChunk)centerChunk).getOrCreateCarvingMask(step);

        for(int dx = -8; dx <= 8; ++dx) {
            for(int dz = -8; dz <= 8; ++dz) {
                var chunk = region.getChunk(centerPos.x + dx, centerPos.z + dz);
                var chunkPos = chunk.getPos();

                int x = chunkPos.getMinBlockX();
                int z = chunkPos.getMinBlockZ();
                var biome = biomeManager.getNoiseBiomeAtQuart(x >> 2, 0, z >> 2);
                var settings = biome.value().getGenerationSettings();

                var carvers = (List<Holder<ConfiguredWorldCarver<?>>>) settings.getCarvers(step);
                for (int i = 0; i < carvers.size(); i++) {
                    var carver = carvers.get(i).value();

                    random.setLargeFeatureSeed(seed + i, chunkPos.x, chunkPos.z);
                    if (random.nextFloat() < 0.5F && carver.isStartChunk(random)) {
                        carver.carve(context, centerChunk, biomeManager::getBiome, random, aquifer, chunkPos, mask);
                    }
                }
            }
        }
    }
}
