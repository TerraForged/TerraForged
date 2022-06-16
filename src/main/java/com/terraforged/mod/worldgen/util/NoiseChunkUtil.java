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

import com.terraforged.mod.util.ReflectionUtil;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.Seeds;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletableFuture;

public class NoiseChunkUtil {
    private static final MethodHandle SURFACE_CACHE = ReflectionUtil.field(NoiseChunk.class, Long2IntMap.class);

    public static NoiseChunk getNoiseChunk(ChunkAccess chunk, RandomState state, Generator generator) {
        var terrainData = generator.getChunkDataAsync(Seeds.get(state), chunk.getPos());

        var noiseChunk = chunk.getOrCreateNoiseChunk(c -> {
            var vanilla = generator.getVanillaGen();
            var fluidPicker = vanilla.getGlobalFluidPicker();
            var settings = vanilla.getSettings().value();
            return NoiseChunk.forChunk(c, state, NoopNoise.BEARDIFIER, settings, fluidPicker, Blender.empty());
        });

        initChunk(chunk, noiseChunk, terrainData);

        return noiseChunk;
    }

    private static void initChunk(ChunkAccess chunk, NoiseChunk noiseChunk, CompletableFuture<TerrainData> terrainData) {
        var cache = getCache(noiseChunk);
        if (!cache.isEmpty()) return;

        initSurfaceCache(chunk, cache, terrainData);
    }

    private static void initSurfaceCache(ChunkAccess chunk, Long2IntMap cache, CompletableFuture<TerrainData> terrainData) {
        var chunkPos = chunk.getPos();
        var data = terrainData.join();

        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        cache.clear();

        for (int dz = 0; dz < 16; dz += 4) {
            for (int dx = 0; dx < 16; dx += 4) {
                int height = data.getHeight(dx, dz);
                int qx = QuartPos.toBlock(QuartPos.fromBlock(startX + dx));
                int qz = QuartPos.toBlock(QuartPos.fromBlock(startZ + dz));
                long index = ColumnPos.asLong(qx, qz);
                cache.put(index, height);
                min = Math.min(min, height);
                max = Math.max(max, height);
            }
        }

        // Note: I don't understand what area of height values vanilla
        // chunk gen expects. Fill a 3x3 chunk area because that seems
        // to fix an issue where some surfaces just get left as stone
        // for some reason. TODO: Figure out wtf

        for (int dz = -16; dz < 32; dz += 4) {
            for (int dx = -16; dx < 32; dx += 4) {
                if ((dx & 15) == dx && (dz & 15) == dz) continue;

                int qx = QuartPos.toBlock(QuartPos.fromBlock(startX + dx));
                int qz = QuartPos.toBlock(QuartPos.fromBlock(startZ + dz));
                long index = ColumnPos.asLong(qx, qz);

                cache.put(index, min);
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
