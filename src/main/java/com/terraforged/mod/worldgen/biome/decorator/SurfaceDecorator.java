package com.terraforged.mod.worldgen.biome.decorator;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.biome.surface.Surface;
import net.minecraft.world.level.chunk.ChunkAccess;

public class SurfaceDecorator {
    public void decorate(ChunkAccess chunk, Generator generator) {
        var chunkData = generator.getChunkData(chunk.getPos());
        Surface.apply(chunkData, chunk, generator);
    }
}
