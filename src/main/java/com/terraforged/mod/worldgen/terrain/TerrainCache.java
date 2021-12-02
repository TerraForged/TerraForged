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

import com.terraforged.mod.util.Timer;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiFunction;

public class TerrainCache {
    private final TerrainGenerator generator;
    private final Timer noiseTimer = new Timer("NOISE_GEN", 10, TimeUnit.SECONDS);
    private final Map<ChunkPos, ForkJoinTask<TerrainData>> cache = new ConcurrentHashMap<>();

    public TerrainCache(TerrainLevels levels, NoiseGenerator noiseGenerator) {
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

    public CompletableFuture<ChunkAccess> hint(ChunkAccess chunk) {
        hint(chunk.getPos());
        return CompletableFuture.completedFuture(chunk);
    }

    public int getHeight(int x, int z) {
        return generator.getHeight(x, z);
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

    public ForkJoinTask<TerrainData> getAsync(ChunkPos pos) {
        return cache.computeIfAbsent(pos, this::compute);
    }

    public <T> CompletableFuture<ChunkAccess> combineAsync(Executor executor,
                                                           ChunkAccess chunkAccess,
                                                           BiFunction<ChunkAccess, TerrainData, ChunkAccess> function) {

        return CompletableFuture.completedFuture(chunkAccess)
                .thenApplyAsync(chunk -> function.apply(chunk, getNow(chunk.getPos())), executor);
    }

    protected ForkJoinTask<TerrainData> compute(ChunkPos pos) {
        return ForkJoinPool.commonPool().submit(() -> generate(pos));
    }

    protected TerrainData generate(ChunkPos pos) {
        try (var timer = noiseTimer.start()) {
            return generator.generate(pos);
        }
    }
}
