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

import com.terraforged.core.region.legacy.LegacyRegion;
import com.terraforged.core.util.concurrent.Disposable;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.core.util.concurrent.cache.CacheEntry;
import com.terraforged.core.world.WorldGenerator;
import com.terraforged.core.world.WorldGeneratorFactory;
import com.terraforged.core.world.heightmap.RegionExtent;
import com.terraforged.core.world.rivermap.RiverRegionList;

import java.util.concurrent.CompletableFuture;

public class RegionGenerator implements RegionExtent {

    private final int factor;
    private final int border;
    private final RegionFactory regions;
    private final ThreadPool threadPool;
    private final ThreadLocal<WorldGenerator> genPool;
    private final Disposable.Listener<Region> disposalListener;

    private RegionGenerator(Builder builder) {
        this.factor = builder.factor;
        this.border = builder.border;
        this.threadPool = builder.threadPool;
        this.regions = builder.regionFactory;
        this.genPool = ThreadLocal.withInitial(builder.factory);
        this.disposalListener = region -> {};
    }

    protected RegionGenerator(RegionGenerator from, Disposable.Listener<Region> listener) {
        this.factor = from.factor;
        this.border = from.border;
        this.threadPool = from.threadPool;
        this.regions = from.regions;
        this.genPool = from.genPool;
        this.disposalListener = listener;
    }

    public RegionCache toCache() {
        return toCache(true);
    }

    public RegionCache toCache(boolean queueNeighbours) {
        return new RegionCache(queueNeighbours, this);
    }

    @Override
    public int chunkToRegion(int i) {
        return i >> factor;
    }

    @Override
    public Region getRegion(int regionX, int regionZ) {
        return generateRegion(regionX, regionZ);
    }

    @Override
    public CompletableFuture<Region> getRegionAsync(int regionX, int regionZ) {
        return generate(regionX, regionZ);
    }

    public CompletableFuture<Region> generate(int regionX, int regionZ) {
        return CompletableFuture.supplyAsync(() -> generateRegion(regionX, regionZ), threadPool);
    }

    public CompletableFuture<Region> generate(float centerX, float centerZ, float zoom, boolean filter) {
        return CompletableFuture.supplyAsync(() -> generateRegion(centerX, centerZ, zoom, filter), threadPool);
    }

    public CacheEntry<Region> generateCached(int regionX, int regionZ) {
        return CacheEntry.supplyAsync(() -> generateRegion(regionX, regionZ), threadPool);
    }

    public Region generateRegion(int regionX, int regionZ) {
        WorldGenerator generator = genPool.get();
        Region region = regions.create(regionX, regionZ, factor, border, disposalListener);
        RiverRegionList rivers = generator.getHeightmap().getRiverMap().getRivers(region);
        region.generateBase(generator.getHeightmap());
        region.generateRivers(generator.getHeightmap(), rivers);
        postProcess(region, generator);
        return region;
    }

    private void postProcess(Region region, WorldGenerator generator) {
        generator.getFilters().apply(region);
        region.decorate(generator.getDecorators().getDecorators());
    }

    public Region generateRegion(float centerX, float centerZ, float zoom, boolean filter) {
        WorldGenerator generator = genPool.get();
        Region region = regions.create(0, 0, factor, border, disposalListener);
        region.generateZoom(generator.getHeightmap(), centerX, centerZ, zoom);
        postProcess(region, generator, centerX, centerZ, zoom, filter);
        return region;
    }

    private void postProcess(Region region, WorldGenerator generator, float centerX, float centerZ, float zoom, boolean filter) {
        if (filter) {
            generator.getFilters().apply(region);
        }
        region.decorateZoom(generator.getDecorators().getDecorators(), centerX, centerZ, zoom);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int factor = 0;
        private int border = 0;
        private ThreadPool threadPool;
        private WorldGeneratorFactory factory;
        private RegionFactory regionFactory = Region::new;

        public Builder size(int factor, int border) {
            return factor(factor).border(border);
        }

        public Builder factor(int factor) {
            this.factor = factor;
            return this;
        }

        public Builder border(int border) {
            this.border = border;
            return this;
        }

        public Builder pool(ThreadPool threadPool) {
            this.threadPool = threadPool;
            return this;
        }

        public Builder regions(RegionFactory factory) {
            this.regionFactory = factory;
            return this;
        }

        public Builder legacy(boolean legacy) {
            if (legacy) {
                return regions(LegacyRegion::new);
            }
            return this;
        }

        public Builder factory(WorldGeneratorFactory factory) {
            this.factory = factory;
            return this;
        }

        public RegionGenerator build() {
            return new RegionGenerator(this);
        }
    }
}
