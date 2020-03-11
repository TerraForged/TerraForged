package com.terraforged.mod.chunk;

import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.core.region.chunk.ChunkReader;
import net.minecraft.util.math.ChunkPos;

public interface ChunkProcessor {

    void preProcess(ChunkPos pos, ChunkReader chunk, TerraBiomeArray container);

    void postProcess(ChunkReader chunk, TerraBiomeArray container, DecoratorContext context);
}
