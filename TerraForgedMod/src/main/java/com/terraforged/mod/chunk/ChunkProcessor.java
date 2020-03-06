package com.terraforged.mod.chunk;

import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.core.region.chunk.ChunkReader;
import net.minecraft.util.math.ChunkPos;

public interface ChunkProcessor {

    void preProcess(ChunkPos pos, ChunkReader chunk, TerraContainer container);

    void postProcess(ChunkReader chunk, TerraContainer container, DecoratorContext context);
}
