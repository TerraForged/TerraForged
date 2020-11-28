/*
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

package com.terraforged.mod.chunk.lite;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.cache.ExpiringEntry;
import com.terraforged.core.filter.Filterable;
import com.terraforged.core.tile.Size;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.core.tile.chunk.ChunkWriter;
import com.terraforged.core.util.PosUtil;
import com.terraforged.world.heightmap.Heightmap;

import java.util.function.LongConsumer;

public class LiteChunk implements ChunkReader, ChunkWriter, Filterable, ExpiringEntry {

    private static final Size SIZE = Size.blocks(0, 0);

    private final int chunkX;
    private final int chunkZ;
    private final int blockX;
    private final int blockZ;
    private final Heightmap heightmap;
    private final Cell lookup = new Cell();
    private final Cell[] backing = new Cell[256];

    private long timestamp = System.currentTimeMillis();

    public LiteChunk(int chunkX, int chunkZ, Heightmap heightmap) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blockX = chunkX << 4;
        this.blockZ = chunkZ << 4;
        this.heightmap = heightmap;
    }

    @Override
    public Cell genCell(int dx, int dz) {
        dx &= 15;
        dz &= 15;
        int index = dz << 4 | dx;
        Cell cell = backing[index];
        if (cell == null) {
            cell = new Cell();
            backing[index] = cell;
        }
        return cell;
    }

    @Override
    public Cell getCell(int dx, int dz) {
        dx &= 15;
        dz &= 15;
        return backing[(dz << 4) | dx];
    }

    @Override
    public void close() {
        timestamp = System.currentTimeMillis();
    }

    @Override
    public void dispose() {

    }

    @Override
    public int getChunkX() {
        return chunkX;
    }

    @Override
    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public int getBlockX() {
        return chunkX << 4;
    }

    @Override
    public int getBlockZ() {
        return chunkZ << 4;
    }

    @Override
    public Size getSize() {
        return SIZE;
    }

    @Override
    public Cell[] getBacking() {
        return backing;
    }

    @Override
    public Cell getCellRaw(int x, int z) {
        int dx = x - blockX;
        int dz = z - blockZ;
        if (dx < 0 || dx > 15 || dz < 0 || dz > 15) {
            Cell cell = lookup;
            heightmap.apply(cell, x, z);
            return cell;
        }
        return getCell(dx, dz);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
