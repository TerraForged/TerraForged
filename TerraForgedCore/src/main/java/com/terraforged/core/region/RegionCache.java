/*
 *   
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.core.region;

import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.util.Cache;
import com.terraforged.core.world.heightmap.RegionExtent;
import me.dags.noise.util.NoiseUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RegionCache implements RegionExtent {

    private final boolean queuing;
    private final RegionGenerator renderer;
    private final Cache<Long, CompletableFuture<Region>> cache;

    private Region cachedRegion = null;

    public RegionCache(boolean queueNeighbours, RegionGenerator renderer) {
        this.renderer = renderer;
        this.queuing = queueNeighbours;
        this.cache = new Cache<>(180, 60, TimeUnit.SECONDS, () -> new ConcurrentHashMap<>());
    }

    @Override
    public int chunkToRegion(int coord) {
        return renderer.chunkToRegion(coord);
    }

    @Override
    public CompletableFuture<Region> getRegionAsync(int regionX, int regionZ) {
        long id = NoiseUtil.seed(regionX, regionZ);
        CompletableFuture<Region> future = cache.get(id);
        if (future == null) {
            future = renderer.getRegionAsync(regionX, regionZ);
            cache.put(id, future);
        }
        return future;
    }

    @Override
    public ChunkReader getChunk(int chunkX, int chunkZ) {
        int regionX = renderer.chunkToRegion(chunkX);
        int regionZ = renderer.chunkToRegion(chunkZ);
        Region region = getRegion(regionX, regionZ);
        return region.getChunk(chunkX, chunkZ);
    }

    @Override
    public Region getRegion(int regionX, int regionZ) {
        if (cachedRegion != null && regionX == cachedRegion.getRegionX() && regionZ == cachedRegion.getRegionZ()) {
            return cachedRegion;
        }

        long id = NoiseUtil.seed(regionX, regionZ);
        CompletableFuture<Region> futureRegion = cache.get(id);

        if (futureRegion == null) {
            cachedRegion = renderer.generateRegion(regionX, regionZ);
            cache.put(id, CompletableFuture.completedFuture(cachedRegion));
        } else {
            cachedRegion = futureRegion.join();
        }

        if (queuing) {
            queueNeighbours(regionX, regionZ);
        }

        return cachedRegion;
    }

    private void queueNeighbours(int regionX, int regionZ) {
        for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++){
                if (x == 0 && z == 0) {
                    continue;
                }
                getRegionAsync(regionX + x, regionZ + z);
            }
        }
    }
}
