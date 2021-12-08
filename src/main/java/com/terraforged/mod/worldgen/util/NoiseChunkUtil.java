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

import com.terraforged.mod.util.ReflectionUtils;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class NoiseChunkUtil {
    private static final MethodHandle SURFACE_CACHE = ReflectionUtils.field(NoiseChunk.class, Long2IntMap.class);
    private static final Supplier<NoiseChunk.NoiseFiller> FILLER = () -> (x, y, z) -> 0;

    public static void initChunk(ChunkAccess chunk, Generator generator) {
        getNoiseChunk(chunk, generator);
    }

    public static NoiseChunk getNoiseChunk(ChunkAccess chunk, Generator generator) {
        var vanilla = generator.getVanillaGen();
        var fluidPicker = vanilla.getGlobalFluidPicker();
        var noiseChunk = chunk.getOrCreateNoiseChunk(vanilla.getNoiseSampler(), FILLER, vanilla.getSettings().get(), fluidPicker, Blender.empty());
        initChunk(chunk, noiseChunk, generator);
        return noiseChunk;
    }

    private static void initChunk(ChunkAccess chunk, NoiseChunk noiseChunk, Generator generator) {
        var cache = getCache(noiseChunk);
        if (!cache.isEmpty()) return;

        var terrainData = generator.getChunkDataAsync(chunk.getPos());

        initSurfaceCache(chunk, cache, terrainData);
    }

    private static void initSurfaceCache(ChunkAccess chunk, Long2IntMap cache, CompletableFuture<TerrainData> terrainData) {
        var chunkPos = chunk.getPos();
        var data = terrainData.join();

        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();
        int lowest = Integer.MAX_VALUE;

        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                int height = data.getHeight(dx, dz);
                int qx = QuartPos.fromBlock(startX + dx);
                int qz = QuartPos.fromBlock(startZ + dz);
                long index = ChunkPos.asLong(qx, qz);

                int current = cache.getOrDefault(index, Integer.MAX_VALUE);
                if (height < current) {
                    cache.put(index, height);
                    lowest = Math.min(lowest, height);
                }
            }
        }

        // Note: I don't understand what area of height values vanilla
        // chunk gen expects. Fill a 3x3 chunk area because that seems
        // to fix an issue where some surfaces just get left as stone
        // for some reason. TODO: Figure out wtf

        for (int dz = -16; dz < 32; dz++) {
            for (int dx = -16; dx < 32; dx++) {
                if ((dx & 15) == dx && (dz & 15) == dz) continue;

                int qx = QuartPos.fromBlock(startX + dx);
                int qz = QuartPos.fromBlock(startZ + dz);
                long index = ChunkPos.asLong(qx, qz);

                cache.put(index, lowest);
            }
        }
    }

    private static Long2IntMap getCache(NoiseChunk noiseChunk) {
        try {
            return (Long2IntMap) SURFACE_CACHE.invokeExact(noiseChunk);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
