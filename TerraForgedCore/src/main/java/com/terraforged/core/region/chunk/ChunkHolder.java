package com.terraforged.core.region.chunk;

import com.terraforged.core.cell.Extent;

public interface ChunkHolder extends Extent {

    int getChunkX();

    int getChunkZ();

    int getBlockX();

    int getBlockZ();
}
