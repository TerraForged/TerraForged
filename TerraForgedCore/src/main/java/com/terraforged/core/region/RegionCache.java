package com.terraforged.core.region;

import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.util.Cache;
import com.terraforged.core.util.FutureValue;
import com.terraforged.core.world.heightmap.RegionExtent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RegionCache implements RegionExtent {

    private final boolean queuing;
    private final RegionGenerator renderer;
    private final Cache<Long, Future<Region>> cache;

    private Region cachedRegion = null;

    public RegionCache(boolean queueNeighbours, RegionGenerator renderer) {
        this.renderer = renderer;
        this.queuing = queueNeighbours;
        this.cache = new com.terraforged.core.util.Cache<>(30, 30, TimeUnit.SECONDS);
    }

    @Override
    public int chunkToRegion(int coord) {
        return renderer.chunkToRegion(coord);
    }

    @Override
    public Future<Region> getRegionAsync(int regionX, int regionZ) {
        long id = NoiseUtil.seed(regionX, regionZ);
        Future<Region> future = cache.get(id);
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
        Future<Region> futureRegion = cache.get(id);

        if (futureRegion == null) {
            cachedRegion = renderer.generateRegion(regionX, regionZ);
            cache.put(id, new FutureValue<>(cachedRegion));
        } else {
            try {
                cachedRegion = futureRegion.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
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
