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
