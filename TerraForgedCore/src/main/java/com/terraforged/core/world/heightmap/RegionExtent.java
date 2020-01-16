package com.terraforged.core.world.heightmap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Extent;
import com.terraforged.core.region.Region;
import com.terraforged.core.region.Size;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.world.terrain.Terrain;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface RegionExtent extends Extent {

    int chunkToRegion(int coord);

    Region getRegion(int regionX, int regionZ);

    Future<Region> getRegionAsync(int regionX, int regionZ);

    default ChunkReader getChunk(int chunkX, int chunkZ) {
        int regionX = chunkToRegion(chunkX);
        int regionZ = chunkToRegion(chunkZ);
        Region region = getRegion(regionX, regionZ);
        return region.getChunk(chunkX, chunkZ);
    }

    default List<Future<Region>> getRegions(int minRegionX, int minRegionZ, int maxRegionX, int maxRegionZ) {
        List<Future<Region>> regions = new LinkedList<>();
        for (int rz = minRegionZ; rz <= maxRegionZ; rz++) {
            for (int rx = minRegionX; rx <= maxRegionX; rx++) {
                regions.add(getRegionAsync(rx, rz));
            }
        }
        return regions;
    }

    @Override
    default void visit(int minX, int minZ, int maxX, int maxZ, Cell.Visitor<Terrain> visitor) {
        int minRegionX = chunkToRegion(Size.blockToChunk(minX));
        int minRegionZ = chunkToRegion(Size.blockToChunk(minZ));
        int maxRegionX = chunkToRegion(Size.blockToChunk(maxX));
        int maxRegionZ = chunkToRegion(Size.blockToChunk(maxZ));
        List<Future<Region>> regions = getRegions(minRegionX, minRegionZ, maxRegionX, maxRegionZ);
        while (!regions.isEmpty()) {
            regions.removeIf(future -> {
                if (!future.isDone()) {
                    return false;
                }
                try {
                    Region region = future.get();
                    region.visit(minX, minZ, maxX, maxZ, visitor);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return true;
            });
        }
    }
}
