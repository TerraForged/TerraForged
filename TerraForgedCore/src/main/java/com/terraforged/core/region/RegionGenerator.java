package com.terraforged.core.region;

import com.terraforged.core.util.concurrent.ObjectPool;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.core.world.WorldGenerator;
import com.terraforged.core.world.WorldGeneratorFactory;
import com.terraforged.core.world.heightmap.RegionExtent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class RegionGenerator implements RegionExtent {

    private final int factor;
    private final int border;
    private final ThreadPool threadPool;
    private final ObjectPool<WorldGenerator> genPool;

    private RegionGenerator(Builder builder) {
        this.factor = builder.factor;
        this.border = builder.border;
        this.threadPool = builder.threadPool;
        this.genPool = new ObjectPool<>(50, builder.factory);
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
    public Future<Region> getRegionAsync(int regionX, int regionZ) {
        return generate(regionX, regionZ);
    }

    public Future<Region> generate(int regionX, int regionZ) {
        return ForkJoinPool.commonPool().submit(() -> generateRegion(regionX, regionZ));
    }

    public Future<Region> generate(float centerX, float centerZ, float zoom, boolean filter) {
        return ForkJoinPool.commonPool().submit(() -> generateRegion(centerX, centerZ, zoom, filter));
    }

    public Region generateRegion(int regionX, int regionZ) {
        try (ObjectPool.Item<WorldGenerator> item = genPool.get()) {
            WorldGenerator generator = item.getValue();
            Region region = new Region(regionX, regionZ, factor, border);
            try (ThreadPool.Batcher batcher = threadPool.batcher(region.getChunkCount())) {
                region.generate(generator.getHeightmap(), batcher);
            }
            postProcess(region, generator);
            return region;
        }
    }

    private void postProcess(Region region, WorldGenerator generator) {
        generator.getFilters().apply(region);
        region.decorate(generator.getDecorators().getDecorators());
    }

    public Region generateRegion(float centerX, float centerZ, float zoom, boolean filter) {
        try (ObjectPool.Item<WorldGenerator> item = genPool.get()) {
            WorldGenerator generator = item.getValue();
            Region region = new Region(0, 0, factor, border);
            try (ThreadPool.Batcher batcher = threadPool.batcher(region.getChunkCount())) {
                region.generateZoom(generator.getHeightmap(), centerX, centerZ, zoom, batcher);
            }
            region.check();
            postProcess(region, generator, centerX, centerZ, zoom, filter);
            return region;
        }
    }

    private void postProcess(Region region, WorldGenerator generator, float centerX, float centerZ, float zoom, boolean filter) {
        if (filter) {
            generator.getFilters().apply(region);
        }
//        region.decorateZoom(generator.getDecorators().getDecorators(), centerX, centerZ, zoom);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int factor = 0;
        private int border = 0;
        private ThreadPool threadPool;
        private WorldGeneratorFactory factory;

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

        public Builder factory(WorldGeneratorFactory factory) {
            this.factory = factory;
            return this;
        }

        public RegionGenerator build() {
            return new RegionGenerator(this);
        }
    }
}
