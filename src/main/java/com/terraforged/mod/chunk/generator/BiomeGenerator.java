package com.terraforged.mod.chunk.generator;

import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.util.TerraContainer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IChunk;

public class BiomeGenerator implements Generator.Biomes {

    private final TerraChunkGenerator generator;

    public BiomeGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void generateBiomes(IChunk chunk) {
        ChunkPos pos = chunk.getPos();
        try (ChunkReader reader = generator.getChunkReader(pos.x, pos.z)) {
            TerraContainer.create(reader, generator.getBiomeProvider());
        }
    }
}
