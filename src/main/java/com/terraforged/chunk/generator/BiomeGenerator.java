package com.terraforged.chunk.generator;

import com.terraforged.chunk.TerraChunkGenerator;
import com.terraforged.chunk.util.TerraContainer;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.world.terrain.decorator.Decorator;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;

public class BiomeGenerator {

    private final TerraChunkGenerator generator;

    public BiomeGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    public void generateBiomes(IChunk chunk) {
        ChunkPos pos = chunk.getPos();
        ChunkReader reader = generator.getChunkReader(pos.x, pos.z);
        TerraContainer container = generator.getBiomeProvider().createBiomeContainer(reader);
        ((ChunkPrimer) chunk).func_225548_a_(container);
        // apply chunk-local heightmap modifications
        preProcess(pos, reader, container);
    }

    private void preProcess(ChunkPos pos, ChunkReader chunk, TerraContainer container) {
        chunk.iterate((cell, dx, dz) -> {
            Biome biome = container.getBiome(dx, dz);
            for (Decorator decorator : generator.getBiomeProvider().getDecorators(biome)) {
                if (decorator.apply(cell, pos.getXStart() + dx, pos.getZStart() + dz)) {
                    return;
                }
            }
        });
    }
}
