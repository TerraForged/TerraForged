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

package com.terraforged.mod.worldgen.util;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.biome.surface.SurfaceRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.util.function.Function;

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
                var settings = biome.getGenerationSettings();

                var carvers = settings.getCarvers(step);
                for (int i = 0; i < carvers.size(); i++) {
                    var carver = carvers.get(i).get();

                    random.setLargeFeatureSeed(seed + i, chunkPos.x, chunkPos.z);
                    if (carver.isStartChunk(random)) {
                        carver.carve(context, centerChunk, biomeManager::getBiome, random, aquifer, chunkPos, mask);
                    }
                }
            }
        }
    }

    private static class CarverBiomeManager implements Function<BlockPos, Biome> {
        private final WorldGenRegion region;
        private final BiomeManager regionBiomes;
        private final BiomeManager worldBiomes;
        private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        private CarverBiomeManager(WorldGenRegion region, Generator generator) {
            this.region = region;
            this.regionBiomes = SurfaceRegion.wrap(region).getBiomeManager();
            this.worldBiomes = region.getBiomeManager().withDifferentSource(generator);
        }

        public Biome getBiome(int x, int y, int z) {
            if (true) {
                return worldBiomes.getBiome(pos.set(x, y, z));
            }

            int chunkX = x >> 4;
            int chunkZ = z >> 4;

            if (region.hasChunk(chunkX, chunkZ)) {
                var status = region.getChunk(chunkX, chunkZ).getStatus();
                if (status.isOrAfter(ChunkStatus.BIOMES)) {
                    return regionBiomes.getBiome(pos.set(x, y, z));
                }
            }

            return worldBiomes.getNoiseBiomeAtPosition(pos.set(x, y, z));
        }

        @Override
        public Biome apply(BlockPos pos) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            if (true) {
                return worldBiomes.getBiome(pos);
            }

            if (region.hasChunk(chunkX, chunkZ)) {
                return regionBiomes.getBiome(pos);
            }

            return worldBiomes.getBiome(pos);
        }
    }
}
