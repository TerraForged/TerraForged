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
import com.terraforged.core.filter.Filterable;
import com.terraforged.core.tile.Size;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.mod.chunk.TFChunkGenerator;

public class LiteFilterable implements Filterable {

    private static final Size CHUNK_SIZE = Size.blocks(0, 0);

    private final LiteChunk center;
    private final TFChunkGenerator generator;

    private int cachedX;
    private int cachedZ;
    private ChunkReader cached = null;

    public LiteFilterable(LiteChunk center, TFChunkGenerator generator) {
        this.center = center;
        this.generator = generator;
    }

    @Override
    public Size getSize() {
        return CHUNK_SIZE;
    }

    @Override
    public Cell[] getBacking() {
        return null;
    }

    @Override
    public Cell getCellRaw(int dx, int dz) {
        int x = center.getBlockX() + dx;
        int z = center.getBlockZ() + dz;
        return getChunk(x >> 4, z >> 4).getCell(x, z);
    }

    private ChunkReader getChunk(int chunkX, int chunkZ) {
        if (chunkX == center.getChunkX() && chunkZ == center.getChunkZ()) {
            return center;
        }

        if (cached != null && chunkX == cachedX && chunkZ == cachedZ) {
            return cached;
        }

        cachedX = chunkX;
        cachedZ = chunkZ;
        cached = generator.getChunkReader(chunkX, chunkZ);
        return cached;
    }
}
