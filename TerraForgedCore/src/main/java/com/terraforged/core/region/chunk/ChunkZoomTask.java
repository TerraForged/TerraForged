package com.terraforged.core.region.chunk;

import com.terraforged.core.world.heightmap.Heightmap;

public class ChunkZoomTask extends ChunkGenTask {

    private final float translateX;
    private final float translateZ;
    private final float zoom;

    public ChunkZoomTask(ChunkWriter chunk, Heightmap heightmap, float translateX, float translateZ, float zoom) {
        super(chunk, heightmap);
        this.translateX = translateX;
        this.translateZ = translateZ;
        this.zoom = zoom;
    }

    @Override
    public void run() {
        chunk.generate((cell, dx, dz) -> {
            float x = ((chunk.getBlockX() + dx) * zoom) + translateX;
            float z = ((chunk.getBlockZ() + dz) * zoom) + translateZ;
            heightmap.apply(cell, x, z);
        });
    }
}
