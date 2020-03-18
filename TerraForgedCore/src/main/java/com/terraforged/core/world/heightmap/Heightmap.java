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
import com.terraforged.core.cell.Populator;
import com.terraforged.core.region.Size;
import com.terraforged.core.util.concurrent.ObjectPool;
import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.river.RiverManager;
import com.terraforged.core.world.river.RiverRegionList;
import com.terraforged.core.world.terrain.Terrain;

public interface Heightmap extends Populator, Extent {

    Climate getClimate();

    RiverManager getRiverManager();

    void visit(Cell<Terrain> cell, float x, float z);

    void applyBase(Cell<Terrain> cell, float x, float z);

    void applyRivers(Cell<Terrain> cell, float x, float z, RiverRegionList rivers);

    void applyClimate(Cell<Terrain> cell, float x, float z);

    @Override
    default void visit(int minX, int minZ, int maxX, int maxZ, Cell.Visitor<Terrain> visitor) {
        int chunkSize = Size.chunkToBlock(1);

        int chunkMinX = Size.blockToChunk(minX);
        int chunkMinZ = Size.blockToChunk(minZ);
        int chunkMaxX = Size.blockToChunk(maxX);
        int chunkMaxZ = Size.blockToChunk(maxZ);

        try (ObjectPool.Item<Cell<Terrain>> cell = Cell.pooled()) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                    int chunkStartX = Size.chunkToBlock(chunkX);
                    int chunkStartZ = Size.chunkToBlock(chunkZ);
                    for (int dz = 0; dz < chunkSize; dz++) {
                        for (int dx = 0; dx < chunkSize; dx++) {
                            int x = chunkStartX + dx;
                            int z = chunkStartZ + dz;
                            apply(cell.getValue(), x, z);
                            if (x >= minX && x < maxX && z >= minZ && z < maxZ) {
                                int relX = x - minX;
                                int relZ = z - minZ;
                                visitor.visit(cell.getValue(), relX, relZ);
                            }
                        }
                    }
                }
            }
        }
    }
}
