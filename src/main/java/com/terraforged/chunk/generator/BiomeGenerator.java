package com.terraforged.chunk.generator;

import com.terraforged.biome.provider.TerraBiomeProvider;
import com.terraforged.chunk.TerraChunkGenerator;
import com.terraforged.chunk.util.TerraContainer;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.world.terrain.decorator.Decorator;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;

public class BiomeGenerator implements Generator.Biomes {

    private final TerraChunkGenerator generator;
    private final TerraBiomeProvider biomeProvider;

    public BiomeGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
        this.biomeProvider = generator.getBiomeProvider();
    }

    @Override
    public void generateBiomes(IChunk chunk) {
        ChunkPos pos = chunk.getPos();
        try (ChunkReader reader = generator.getChunkReader(pos.x, pos.z)) {
            TerraContainer container = TerraContainer.create(reader, generator.getBiomeProvider());
            // apply chunk-local heightmap modifications
            preProcess(reader, container);
        }
    }

    private void preProcess(ChunkReader reader, TerraContainer biomes) {
        reader.iterate((cell, dx, dz) -> {
            Biome biome = biomes.getBiome(dx, dz);
            for (Decorator decorator : generator.getBiomeProvider().getDecorators(biome)) {
                if (decorator.apply(cell, reader.getBlockX() + dx, reader.getBlockZ() + dz)) {
                    return;
                }
            }
        });
    }
}
