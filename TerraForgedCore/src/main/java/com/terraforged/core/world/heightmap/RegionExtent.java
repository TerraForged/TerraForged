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

package com.terraforged.core.world.heightmap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Extent;
import com.terraforged.core.region.Region;
import com.terraforged.core.region.Size;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.world.terrain.Terrain;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RegionExtent extends Extent {

    int chunkToRegion(int coord);

    Region getRegion(int regionX, int regionZ);

    CompletableFuture<Region> getRegionAsync(int regionX, int regionZ);

    default ChunkReader getChunk(int chunkX, int chunkZ) {
        int regionX = chunkToRegion(chunkX);
        int regionZ = chunkToRegion(chunkZ);
        Region region = getRegion(regionX, regionZ);
        return region.getChunk(chunkX, chunkZ);
    }

    default List<CompletableFuture<Region>> getRegions(int minRegionX, int minRegionZ, int maxRegionX, int maxRegionZ) {
        List<CompletableFuture<Region>> regions = new LinkedList<>();
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
        List<CompletableFuture<Region>> regions = getRegions(minRegionX, minRegionZ, maxRegionX, maxRegionZ);
        while (!regions.isEmpty()) {
            regions.removeIf(future -> {
                if (!future.isDone()) {
                    return false;
                }
                Region region = future.join();
                region.visit(minX, minZ, maxX, maxZ, visitor);
                return true;
            });
        }
    }
}
