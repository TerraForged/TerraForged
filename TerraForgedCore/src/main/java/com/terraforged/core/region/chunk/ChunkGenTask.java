package com.terraforged.core.region.chunk;

import com.terraforged.core.world.heightmap.Heightmap;

public class ChunkGenTask implements Runnable {

    protected final ChunkWriter chunk;
    protected final Heightmap heightmap;

    public ChunkGenTask(ChunkWriter chunk, Heightmap heightmap) {
        this.chunk = chunk;
        this.heightmap = heightmap;
    }

    @Override
    public void run() {
        chunk.generate((cell, dx, dz) -> {
            float x = chunk.getBlockX() + dx;
            float z = chunk.getBlockZ() + dz;
            heightmap.apply(cell, x, z);
        });
    }
}
