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

package com.terraforged.core.region.chunk;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;

public interface ChunkReader extends ChunkHolder {

    Cell<Terrain> getCell(int dx, int dz);

    @Override
    default void visit(int minX, int minZ, int maxX, int maxZ, Cell.Visitor<Terrain> visitor) {
        int regionMinX = getBlockX();
        int regionMinZ = getBlockZ();
        if (maxX < regionMinX || maxZ < regionMinZ) {
            return;
        }

        int regionMaxX = getBlockX() + 15;
        int regionMaxZ = getBlockZ() + 15;
        if (minX > regionMaxX || maxZ > regionMaxZ) {
            return;
        }

        minX = Math.max(minX, regionMinX);
        minZ = Math.max(minZ, regionMinZ);
        maxX = Math.min(maxX, regionMaxX);
        maxZ = Math.min(maxZ, regionMaxZ);

        for (int z = minZ; z <= maxX; z++) {
            for (int x = minX; x <= maxZ; x++) {
                visitor.visit(getCell(x, z), x, z);
            }
        }
    }

    default void iterate(Cell.Visitor<Terrain> visitor) {
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                visitor.visit(getCell(dx, dz), dx, dz);
            }
        }
    }

    default <C> void iterate(C context, Cell.ContextVisitor<C, Terrain> visitor) {
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                visitor.visit(getCell(dx, dz), dx, dz, context);
            }
        }
    }
}
