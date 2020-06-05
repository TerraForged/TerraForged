package com.terraforged.chunk.generator;

import com.terraforged.chunk.TerraChunkGenerator;
import com.terraforged.chunk.util.TerraContainer;
import com.terraforged.world.terrain.decorator.Decorator;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;

public class BiomeGenerator {

    private final TerraChunkGenerator generator;

    public BiomeGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    public void generateBiomes(IChunk chunk) {
        ChunkPos pos = chunk.getPos();
        TerraContainer container = TerraContainer.getOrCreate(chunk, generator);
        // apply chunk-local heightmap modifications
        preProcess(pos, container);
    }

    private void preProcess(ChunkPos pos, TerraContainer container) {
        container.getChunkReader().iterate((cell, dx, dz) -> {
            Biome biome = container.getBiome(dx, dz);
            for (Decorator decorator : generator.getBiomeProvider().getDecorators(biome)) {
                if (decorator.apply(cell, pos.getXStart() + dx, pos.getZStart() + dz)) {
                    return;
                }
            }
        });
    }
}
