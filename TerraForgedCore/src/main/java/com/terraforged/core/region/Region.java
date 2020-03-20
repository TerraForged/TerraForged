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

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Extent;
import com.terraforged.core.filter.Filterable;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.region.chunk.ChunkWriter;
import com.terraforged.core.util.concurrent.Disposable;
import com.terraforged.core.world.decorator.Decorator;
import com.terraforged.core.world.heightmap.Heightmap;
import com.terraforged.core.world.rivermap.RiverRegionList;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.util.NoiseUtil;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Region implements Extent, Disposable {

    private final int regionX;
    private final int regionZ;
    private final int chunkX;
    private final int chunkZ;
    private final int blockX;
    private final int blockZ;
    private final int border;
    private final Size blockSize;
    private final Size chunkSize;
    private final GenCell[] blocks;
    private final GenChunk[] chunks;
    private final int disposableChunks;
    private final Disposable.Listener<Region> disposalListener;
    private final AtomicInteger disposedChunks = new AtomicInteger();

    public Region(int regionX, int regionZ, int size, int borderChunks) {
        this(regionX, regionZ, size, borderChunks, region -> {});
    }

    public Region(int regionX, int regionZ, int size, int borderChunks, Disposable.Listener<Region> disposalListener) {
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.chunkX = regionX << size;
        this.chunkZ = regionZ << size;
        this.blockX = Size.chunkToBlock(chunkX);
        this.blockZ = Size.chunkToBlock(chunkZ);
        this.border = borderChunks;
        this.chunkSize = Size.chunks(size, borderChunks);
        this.blockSize = Size.blocks(size, borderChunks);
        this.disposalListener = disposalListener;
        this.disposableChunks = chunkSize.size * chunkSize.size;
        this.blocks = new GenCell[blockSize.total * blockSize.total];
        this.chunks = new GenChunk[chunkSize.total * chunkSize.total];
    }

    @Override
    public void dispose() {
        int disposed = disposedChunks.incrementAndGet();
        if (disposed < disposableChunks) {
            return;
        }
        disposalListener.onDispose(this);
    }

    public long getRegionId() {
        return NoiseUtil.seed(getRegionX(), getRegionZ());
    }

    public int getRegionX() {
        return regionX;
    }

    public int getRegionZ() {
        return regionZ;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public int getOffsetChunks() {
        return border;
    }

    public int getChunkCount() {
        return chunks.length;
    }

    public int getBlockCount() {
        return blocks.length;
    }

    public Size getChunkSize() {
        return chunkSize;
    }

    public Size getBlockSize() {
        return blockSize;
    }

    public Filterable<Terrain> filterable() {
        return new FilterRegion();
    }

    public Cell<Terrain> getCell(int blockX, int blockZ) {
        int relBlockX = blockSize.border + blockSize.mask(blockX);
        int relBlockZ = blockSize.border + blockSize.mask(blockZ);
        int index = blockSize.indexOf(relBlockX, relBlockZ);
        return blocks[index];
    }

    public Cell<Terrain> getRawCell(int blockX, int blockZ) {
        int index = blockSize.indexOf(blockX, blockZ);
        return blocks[index];
    }

    public ChunkReader getChunk(int chunkX, int chunkZ) {
        int relChunkX = chunkSize.border + chunkSize.mask(chunkX);
        int relChunkZ = chunkSize.border + chunkSize.mask(chunkZ);
        int index = chunkSize.indexOf(relChunkX, relChunkZ);
        return chunks[index];
    }

    public void generate(Consumer<ChunkWriter> consumer) {
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                consumer.accept(chunk);
            }
        }
    }

    public void generateBase(Heightmap heightmap) {
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                for (int dz = 0; dz < 16; dz++) {
                    for (int dx = 0; dx < 16; dx++) {
                        float x = chunk.getBlockX() + dx;
                        float z = chunk.getBlockZ() + dz;
                        Cell<Terrain> cell = chunk.genCell(dx, dz);
                        heightmap.applyBase(cell, x, z);
                    }
                }
            }
        }
    }

    public void generateRivers(Heightmap heightmap, RiverRegionList rivers) {
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                for (int dz = 0; dz < 16; dz++) {
                    for (int dx = 0; dx < 16; dx++) {
                        float x = chunk.getBlockX() + dx;
                        float z = chunk.getBlockZ() + dz;
                        Cell<Terrain> cell = chunk.genCell(dx, dz);
                        heightmap.applyRivers(cell, x, z, rivers);
                        heightmap.applyClimate(cell, x, z);
                    }
                }
            }
        }
    }

    public void generateZoom(Heightmap heightmap, float offsetX, float offsetZ, float zoom) {
        float translateX = offsetX - ((blockSize.size * zoom) / 2F);
        float translateZ = offsetZ - ((blockSize.size * zoom) / 2F);
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                for (int dz = 0; dz < 16; dz++) {
                    for (int dx = 0; dx < 16; dx++) {
                        float x = ((chunk.getBlockX() + dx) * zoom) + translateX;
                        float z = ((chunk.getBlockZ() + dz) * zoom) + translateZ;
                        Cell<Terrain> cell = chunk.genCell(dx, dz);
                        heightmap.apply(cell, x, z);
                    }
                }
            }
        }
    }

    public void decorate(Collection<Decorator> decorators) {
        for (int dz = 0; dz < blockSize.total; dz++) {
            for (int dx = 0; dx < blockSize.total; dx++) {
                int index = blockSize.indexOf(dx, dz);
                GenCell cell = blocks[index];
                for (Decorator decorator : decorators) {
                    if (decorator.apply(cell, getBlockX() + dx, getBlockZ() + dz)) {
                        break;
                    }
                }
            }
        }
    }

    public void decorateZoom(Collection<Decorator> decorators, float offsetX, float offsetZ, float zoom) {
        float translateX = offsetX - ((blockSize.size * zoom) / 2F);
        float translateZ = offsetZ - ((blockSize.size * zoom) / 2F);
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                chunk.iterate((cell, dx, dz) -> {
                    float x = ((chunk.getBlockX() + dx) * zoom) + translateX;
                    float z = ((chunk.getBlockZ() + dz) * zoom) + translateZ;
                    for (Decorator decorator : decorators) {
                        decorator.apply(cell, x, z);
                    }
                });
            }
        }
    }

    public void iterate(Consumer<ChunkReader> consumer) {
        for (int cz = 0; cz < chunkSize.size; cz++) {
            int chunkZ = chunkSize.border + cz;
            for (int cx = 0; cx < chunkSize.size; cx++) {
                int chunkX = chunkSize.border + cx;
                int index = chunkSize.indexOf(chunkX, chunkZ);
                GenChunk chunk = chunks[index];
                consumer.accept(chunk);
            }
        }
    }

    public void iterate(Cell.Visitor<Terrain> visitor) {
        for (int dz = 0; dz < blockSize.size; dz++) {
            int z = blockSize.border + dz;
            for (int dx = 0; dx < blockSize.size; dx++) {
                int x = blockSize.border + dx;
                int index = blockSize.indexOf(x, z);
                GenCell cell = blocks[index];
                visitor.visit(cell, dx, dz);
            }
        }
    }

    @Override
    public void visit(int minX, int minZ, int maxX, int maxZ, Cell.Visitor<Terrain> visitor) {
        int regionMinX = getBlockX();
        int regionMinZ = getBlockZ();
        if (maxX < regionMinX || maxZ < regionMinZ) {
            return;
        }

        int regionMaxX = getBlockX() + getBlockSize().size - 1;
        int regionMaxZ = getBlockZ() + getBlockSize().size - 1;
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

    private GenChunk computeChunk(int index, int chunkX, int chunkZ) {
        GenChunk chunk = chunks[index];
        if (chunk == null) {
            chunk = new GenChunk(chunkX, chunkZ);
            chunks[index] = chunk;
        }
        return chunk;
    }

    private GenCell computeCell(int index) {
        GenCell cell = blocks[index];
        if (cell == null) {
            cell = new GenCell();
            blocks[index] = cell;
        }
        return cell;
    }

    private static class GenCell extends Cell<Terrain> {}

    private class GenChunk implements ChunkReader, ChunkWriter {

        private final int chunkX;
        private final int chunkZ;
        private final int blockX;
        private final int blockZ;
        private final int regionBlockX;
        private final int regionBlockZ;

        // the coordinate of the chunk within this region (relative to 0,0)
        private GenChunk(int regionChunkX, int regionChunkZ) {
            // the block coordinate of this chunk within this region (relative 0,0)
            this.regionBlockX = regionChunkX << 4;
            this.regionBlockZ = regionChunkZ << 4;
            // the real coordinate of this chunk within the world
            this.chunkX = Region.this.chunkX + regionChunkX - getOffsetChunks();
            this.chunkZ = Region.this.chunkZ + regionChunkZ - getOffsetChunks();
            // the real block coordinate of this chunk within the world
            this.blockX = chunkX << 4;
            this.blockZ = chunkZ << 4;
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
            return blockX;
        }

        @Override
        public int getBlockZ() {
            return blockZ;
        }

        @Override
        public void dispose() {
            Region.this.dispose();
        }

        @Override
        public Cell<Terrain> getCell(int blockX, int blockZ) {
            int relX = regionBlockX + (blockX & 15);
            int relZ = regionBlockZ + (blockZ & 15);
            int index = blockSize.indexOf(relX, relZ);
            return blocks[index];
        }

        @Override
        public Cell<Terrain> genCell(int blockX, int blockZ) {
            int relX = regionBlockX + (blockX & 15);
            int relZ = regionBlockZ + (blockZ & 15);
            int index = blockSize.indexOf(relX, relZ);
            return computeCell(index);
        }
    }

    private class FilterRegion implements Filterable<Terrain> {

        @Override
        public Size getSize() {
            return blockSize;
        }

        @Override
        public Cell<Terrain>[] getBacking() {
            return blocks;
        }

        @Override
        public Cell<Terrain> getCellRaw(int x, int z) {
            int index = blockSize.indexOf(x, z);
            if (index < 0 || index >= blocks.length) {
                return Cell.empty();
            }
            return blocks[index];
        }
    }
}
