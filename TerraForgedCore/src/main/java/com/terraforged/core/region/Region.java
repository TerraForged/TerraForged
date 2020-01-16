package com.terraforged.core.region;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Extent;
import com.terraforged.core.decorator.Decorator;
import com.terraforged.core.filter.Filterable;
import com.terraforged.core.region.chunk.ChunkGenTask;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.region.chunk.ChunkWriter;
import com.terraforged.core.region.chunk.ChunkZoomTask;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.core.world.heightmap.Heightmap;
import com.terraforged.core.world.terrain.Terrain;

import java.util.Collection;
import java.util.function.Consumer;

public class Region implements Extent {

    private final int regionX;
    private final int regionZ;
    private final int chunkX;
    private final int chunkZ;
    private final int blockX;
    private final int blockZ;
    private final Size blockSize;
    private final Size chunkSize;
    private final GenCell[] blocks;
    private final GenChunk[] chunks;

    public Region(int regionX, int regionZ, int size, int borderChunks) {
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.chunkX = regionX << size;
        this.chunkZ = regionZ << size;
        this.blockX = Size.chunkToBlock(chunkX);
        this.blockZ = Size.chunkToBlock(chunkZ);
        this.chunkSize = Size.chunks(size, borderChunks);
        this.blockSize = Size.blocks(size, borderChunks);
        this.blocks = new GenCell[blockSize.total * blockSize.total];
        this.chunks = new GenChunk[chunkSize.total * chunkSize.total];
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

    @Override
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

    public void generate(Heightmap heightmap, ThreadPool.Batcher batcher) {
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                Runnable task = new ChunkGenTask(chunk, heightmap);
                batcher.submit(task);
            }
        }
    }

    public void generateZoom(Heightmap heightmap, float offsetX, float offsetZ, float zoom, ThreadPool.Batcher batcher) {
        float translateX = offsetX - ((blockSize.total * zoom) / 2F);
        float translateZ = offsetZ - ((blockSize.total * zoom) / 2F);
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                Runnable task = new ChunkZoomTask(chunk, heightmap, translateX, translateZ, zoom);
                batcher.submit(task);
            }
        }
    }

    public void decorate(Collection<Decorator> decorators) {
        for (int dz = 0; dz < blockSize.total; dz++) {
            for (int dx = 0; dx < blockSize.total; dx++) {
                int index = blockSize.indexOf(dx, dz);
                GenCell cell = blocks[index];
                for (Decorator decorator : decorators) {
                    decorator.apply(cell, getBlockX() + dx, getBlockZ() + dz);
                }
            }
        }
    }

    public void decorateZoom(Collection<Decorator> decorators, float offsetX, float offsetZ, float zoom) {
        float translateX = offsetX - ((blockSize.total * zoom) / 2F);
        float translateZ = offsetZ - ((blockSize.total * zoom) / 2F);
        for (int dz = 0; dz < blockSize.total; dz++) {
            for (int dx = 0; dx < blockSize.total; dx++) {
                int index = blockSize.indexOf(dx, dz);
                GenCell cell = blocks[index];
                for (Decorator decorator : decorators) {
                    decorator.apply(cell, getBlockX() + translateX + dx, getBlockZ() + translateZ + dz);
                }
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
        private final int relBlockX;
        private final int relBlockZ;

        private GenChunk(int relChunkX, int relChunkZ) {
            this.relBlockX = relChunkX << 4;
            this.relBlockZ = relChunkZ << 4;
            this.chunkX = Region.this.chunkX + relChunkX;
            this.chunkZ = Region.this.chunkZ + relChunkZ;
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
        public Cell<Terrain> getCell(int blockX, int blockZ) {
            int relX = relBlockX + (blockX & 15);
            int relZ = relBlockZ + (blockZ & 15);
            int index = blockSize.indexOf(relX, relZ);
            return blocks[index];
        }

        @Override
        public Cell<Terrain> genCell(int blockX, int blockZ) {
            int relX = relBlockX + (blockX & 15);
            int relZ = relBlockZ + (blockZ & 15);
            int index = blockSize.indexOf(relX, relZ);
            return computeCell(index);
        }
    }

    private class FilterRegion implements Filterable<Terrain> {

        @Override
        public int getRawWidth() {
            return blockSize.total;
        }

        @Override
        public int getRawHeight() {
            return blockSize.total;
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
