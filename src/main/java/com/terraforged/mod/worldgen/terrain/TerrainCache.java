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

package com.terraforged.mod.worldgen.terrain;

import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.util.ThreadPool;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

public class TerrainCache {
    private final TerrainGenerator generator;
    private final Map<ChunkPos, CompletableFuture<TerrainData>> cache = new ConcurrentHashMap<>();

    public TerrainCache(TerrainLevels levels, INoiseGenerator noiseGenerator) {
        this.generator = new TerrainGenerator(levels, noiseGenerator);
    }

    public void drop(ChunkPos pos) {
        var task = cache.remove(pos);
        if (task == null || task.isDone()) return;

        generator.restore(task.join());
    }

    public void hint(ChunkPos pos) {
        getAsync(pos);
    }

    public int getHeight(int x, int z) {
        return generator.getHeight(x, z);
    }

    public NoiseSample getSample(int x, int z) {
        return generator.noiseGenerator.getNoiseSample(x, z);
    }

    public void sample(int x, int z, NoiseSample sample) {
        generator.getNoiseGenerator().sample(x, z, sample);
    }

    public TerrainData getNow(ChunkPos pos) {
        return getAsync(pos).join();
    }

    @Nullable
    public TerrainData getIfReady(ChunkPos pos) {
        var task = cache.get(pos);
        if (task == null || !task.isDone()) return null;

        return task.join();
    }

    public CompletableFuture<TerrainData> getAsync(ChunkPos pos) {
        return cache.computeIfAbsent(pos, this::generate);
    }

    public <T> CompletableFuture<ChunkAccess> combineAsync(Executor executor,
                                                           ChunkAccess chunk,
                                                           BiFunction<ChunkAccess, TerrainData, ChunkAccess> function) {

        return getAsync(chunk.getPos()).thenApplyAsync(terrainData -> function.apply(chunk, terrainData), executor);
    }

    protected CompletableFuture<TerrainData> generate(ChunkPos pos) {
        return CompletableFuture.supplyAsync(() -> generator.generate(pos.x, pos.z), ThreadPool.EXECUTOR);
    }
}
