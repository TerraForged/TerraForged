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

import com.terraforged.mod.util.storage.ObjectPool;
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
import java.util.function.Supplier;

public class TerrainCache {
    private final TerrainGenerator generator;
    private final Map<CacheKey, CacheValue> cache = new ConcurrentHashMap<>(512);

    private final ThreadLocal<CacheKey> localKey = ThreadLocal.withInitial(CacheKey::new);
    private final ObjectPool<CacheKey> keyPool = new ObjectPool<>(512, CacheKey::new);
    private final ObjectPool<CacheValue> valuePool = new ObjectPool<>(512, CacheValue::new);

    public TerrainCache(TerrainLevels levels, INoiseGenerator noiseGenerator) {
        this.generator = new TerrainGenerator(levels, noiseGenerator);
    }

    protected CacheKey allocPos(int seed, ChunkPos pos) {
        return keyPool.take().set(seed, pos.x, pos.z);
    }

    protected CacheKey lookupPos(int seed, ChunkPos pos) {
        return localKey.get().set(seed, pos.x, pos.z);
    }

    public void drop(int seed, ChunkPos pos) {
        var key = lookupPos(seed, pos);
        var value = cache.remove(key);

        if (value == null || value.task == null) return;

        generator.restore(value.task.join());

        keyPool.restore(value.key);

        valuePool.restore(value.reset());
    }

    public void hint(int seed, ChunkPos pos) {
        getAsync(seed, pos);
    }

    public int getHeight(int seed, int x, int z) {
        return generator.getHeight(seed, x, z);
    }

    public NoiseSample getSample(int seed, int x, int z) {
        return generator.noiseGenerator.getNoiseSample(seed, x, z);
    }

    public void sample(int seed, int x, int z, NoiseSample sample) {
        generator.getNoiseGenerator().sample(seed, x, z, sample);
    }

    public TerrainData getNow(int seed, ChunkPos pos) {
        return getAsync(seed, pos).join();
    }

    @Nullable
    public TerrainData getIfReady(int seed, ChunkPos pos) {
        var key = allocPos(seed, pos);
        var value = cache.get(key);

        if (value == null || !value.task.isDone()) return null;

        return value.task.join();
    }

    public CompletableFuture<TerrainData> getAsync(int seed, ChunkPos pos) {
        var key = allocPos(seed, pos);
        return cache.computeIfAbsent(key, this::generate).task;
    }

    public <T> CompletableFuture<ChunkAccess> combineAsync(Executor executor,
                                                           int seed,
                                                           ChunkAccess chunk,
                                                           BiFunction<ChunkAccess, TerrainData, ChunkAccess> function) {

        return getAsync(seed, chunk.getPos()).thenApplyAsync(terrainData -> function.apply(chunk, terrainData), executor);
    }

    protected CacheValue generate(CacheKey key) {
        var value = valuePool.take();
        value.key = key;
        value.generator = generator;
        value.task = CompletableFuture.supplyAsync(value, ThreadPool.EXECUTOR);
        return value;
    }

    protected static class CacheKey {
        protected int seed, x, z;

        public CacheKey set(int seed, int x, int y) {
            this.seed = seed;
            this.x = x;
            this.z = y;
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CacheKey pos && seed == pos.seed && x == pos.x && z == pos.z;
        }

        @Override
        public int hashCode() {
            int result = seed;
            result = 31 * result + x;
            result = 31 * result + z;
            return result;
        }
    }

    protected static class CacheValue implements Supplier<TerrainData> {
        protected CacheKey key;
        protected TerrainGenerator generator;
        protected CompletableFuture<TerrainData> task;

        public CacheValue reset() {
            key = null;
            task = null;
            generator = null;
            return this;
        }

        @Override
        public TerrainData get() {
            return generator.generate(key.seed, key.x, key.z);
        }
    }
}
