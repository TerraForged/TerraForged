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

package com.terraforged.mod.worldgen.noise.erosion;

import com.terraforged.engine.settings.FilterSettings;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.util.ObjectPool;
import com.terraforged.mod.util.map.LossyCache;
import com.terraforged.mod.worldgen.noise.*;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.mod.worldgen.util.ThreadPool;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ErodedNoiseGenerator implements INoiseGenerator {
    private static final int CACHE_SIZE = 256;
    private static final Supplier<float[]> CHUNK_ALLOCATOR = () -> new float[16 * 16];
    private static final IntFunction<CompletableFuture<float[]>[]> CHUNK_TASK_ALLOCATOR = CompletableFuture[]::new;

    protected final NoiseTileSize tileSize;
    protected final ErosionFilter erosion;
    protected final NoiseGenerator generator;
    protected final ThreadLocal<NoiseSample> localSample;
    protected final ThreadLocal<NoiseResource> localResource;

    protected final ObjectPool<float[]> pool;
    protected final LossyCache<CompletableFuture<float[]>> cache;

    public ErodedNoiseGenerator(long seed, NoiseTileSize tileSize, NoiseGenerator generator) {
        var settings = new FilterSettings.Erosion();
        settings.dropletsPerChunk = 350;

        this.tileSize = tileSize;
        this.generator = generator;
        this.erosion = new ErosionFilter((int) seed, tileSize.regionLength, settings);
        this.localSample = ThreadLocal.withInitial(NoiseSample::new);
        this.localResource = ThreadLocal.withInitial(() -> new NoiseResource(tileSize));
        this.pool = new ObjectPool<>(CACHE_SIZE, CHUNK_ALLOCATOR);
        this.cache = LossyCache.concurrent(CACHE_SIZE, CHUNK_TASK_ALLOCATOR, this::restore);
    }

    @Override
    public INoiseGenerator with(long seed, TerrainLevels levels) {
        return generator.with(seed, levels).withErosion();
    }

    @Override
    public NoiseLevels getLevels() {
        return generator.getLevels();
    }

    @Override
    public ContinentNoise getContinent() {
        return generator.getContinent();
    }

    @Override
    public NoiseSample getNoiseSample(int x, int z) {
        return generator.getNoiseSample(x, z);
    }

    @Override
    public float getHeightNoise(int x, int z) {
        return generator.getHeightNoise(x, z);
    }

    @Override
    public long find(int x, int z, int minRadius, int maxRadius, Terrain terrain) {
        return generator.find(x, z, minRadius, maxRadius, terrain);
    }

    @Override
    public void generate(int chunkX, int chunkZ, Consumer<NoiseData> consumer) {
        try {
            var resource = localResource.get();

            collectNeighbours(chunkX, chunkZ, resource);
            generateCenterChunk(chunkX, chunkZ, resource);
            awaitNeighbours(resource);

            generateErosion(chunkX, chunkZ, resource);
            generateRivers(chunkX, chunkZ, resource);

            consumer.accept(resource.chunk);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected void collectNeighbours(int chunkX, int chunkZ, NoiseResource resource) {
        for (int dz = tileSize.chunkMin; dz < tileSize.chunkMax; dz++) {
            for (int dx = tileSize.chunkMin; dx < tileSize.chunkMax; dx++) {
                if (dx == 0 && dz == 0) continue;

                int tileIndex = tileSize.chunkIndexOfRel(dx, dz);

                int cx = chunkX + dx;
                int cz = chunkZ + dz;
                resource.chunkCache[tileIndex] = getChunk(cx, cz);
            }
        }
    }

    protected void generateCenterChunk(int chunkX, int chunkZ, NoiseResource resource) {
        var blender = generator.getBlenderResource();

        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        int min = resource.chunk.min();
        int max = resource.chunk.max();

        for (int dz = min; dz < max; dz++) {
            float nz = getNoiseCoord(startZ + dz);

            for (int dx = min; dx < max; dx++) {
                float nx = getNoiseCoord(startX + dx);

                var sample = resource.chunkSample.get(dx, dz);
                generator.sampleTerrain(nx, nz, sample, blender);

                int tileIndex = tileSize.indexOfRel(dx, dz);
                resource.heightmap[tileIndex] = sample.heightNoise;
            }
        }
    }

    protected void awaitNeighbours(NoiseResource resource) {
        for (int cz = tileSize.chunkMin; cz < tileSize.chunkMax; cz++) {
            for (int cx = tileSize.chunkMin; cx < tileSize.chunkMax; cx++) {
                if (cx == 0 && cz == 0) continue;

                int chunkIndex = tileSize.chunkIndexOfRel(cx, cz);
                float[] chunk = resource.chunkCache[chunkIndex].join();

                int relStartX = cx << 4;
                int relStartZ = cz << 4;
                for (int i = 0; i < chunk.length; i++) {
                    int dx = i & 15;
                    int dz = i >> 4;
                    int index = tileSize.indexOfRel(relStartX + dx, relStartZ + dz);
                    resource.heightmap[index] = chunk[i];
                }
            }
        }
    }

    protected void generateErosion(int chunkX, int chunkZ, NoiseResource resource) {
        erosion.apply(resource.heightmap, chunkX, chunkZ, tileSize, resource.erosionResource, resource.random);
    }

    protected void generateRivers(int chunkX, int chunkZ, NoiseResource resource) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        int min = resource.chunk.min();
        int max = resource.chunk.max();
        for (int dz = min; dz < max; dz++) {
            float nz = getNoiseCoord(startZ + dz);

            for (int dx = min; dx < max; dx++) {
                float nx = getNoiseCoord(startX + dx);

                int tileIndex = tileSize.indexOfRel(dx, dz);
                float height = resource.heightmap[tileIndex];

                int chunkIndex = resource.chunk.index().of(dx, dz);
                var sample = resource.chunkSample.get(chunkIndex);
                sample.heightNoise = height;

                generator.sampleRiver(nx, nz, sample, resource.riverCache);

                resource.chunk.setNoise(chunkIndex, sample);
            }
        }
    }

    protected void restore(CompletableFuture<float[]> task) {
        task.thenAccept(pool::restore);
    }

    protected CompletableFuture<float[]> getChunk(int x, int z) {
        return cache.computeIfAbsent(PosUtil.pack(x, z), this::generateChunk);
    }

    protected CompletableFuture<float[]> generateChunk(final long key) {
        return CompletableFuture.supplyAsync(() -> {
            int chunkX = PosUtil.unpackLeft(key);
            int chunkZ = PosUtil.unpackRight(key);

            int startX = chunkX << 4;
            int startZ = chunkZ << 4;

            float[] height = pool.take();
            var sample = localSample.get();
            var blender = generator.getBlenderResource();

            for (int i = 0; i < height.length; i++) {
                int dx = i & 15;
                int dz = i >> 4;

                float nx = getNoiseCoord(startX + dx);
                float nz = getNoiseCoord(startZ + dz);
                height[i] = generator.sampleTerrain(nx, nz, sample, blender).heightNoise;
            }

            return height;
        }, ThreadPool.EXECUTOR);
    }
}
