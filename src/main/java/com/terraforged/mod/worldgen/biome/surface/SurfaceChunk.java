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

package com.terraforged.mod.worldgen.biome.surface;

import com.terraforged.mod.util.ReflectionUtils;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.mod.worldgen.util.delegate.DelegateChunk;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SurfaceChunk extends DelegateChunk {
    private static final MethodHandle SURFACE_CACHE = ReflectionUtils.field(NoiseChunk.class, Long2IntMap.class);
    private static final ThreadLocal<SurfaceChunk> LOCAL_SURFACE_CHUNK = ThreadLocal.withInitial(SurfaceChunk::new);

    protected NoiseChunk noiseChunk;
    protected CompletableFuture<TerrainData> terrainData;

    SurfaceChunk set(ChunkAccess chunk, CompletableFuture<TerrainData> terrainData) {
        super.set(chunk);
        this.noiseChunk = null;
        this.terrainData = terrainData;
        return this;
    }

    @Override
    public NoiseChunk getOrCreateNoiseChunk(NoiseSampler noise,
                                            Supplier<NoiseChunk.NoiseFiller> filler,
                                            NoiseGeneratorSettings settings,
                                            Aquifer.FluidPicker fluid,
                                            Blender blender) {

        if (noiseChunk != null) return noiseChunk;

        noiseChunk = super.getOrCreateNoiseChunk(noise, filler, settings, fluid, blender);

        return initNoiseChunk(noiseChunk);
    }

    protected NoiseChunk initNoiseChunk(NoiseChunk noiseChunk) {
        var cache = getCache(noiseChunk);
        var chunkPos = delegate.getPos();
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

        return noiseChunk;
    }

    private static Long2IntMap getCache(NoiseChunk noiseChunk) {
        try {
            return (Long2IntMap) SURFACE_CACHE.invokeExact(noiseChunk);
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public static ChunkAccess assign(ChunkAccess chunk, CompletableFuture<TerrainData> terrainData) {
        return LOCAL_SURFACE_CHUNK.get().set(chunk, terrainData);
    }
}
