package com.terraforged.chunk.generator;

import com.terraforged.chunk.TerraChunkGenerator;
import com.terraforged.chunk.fix.ChunkCarverFix;
import com.terraforged.fm.template.StructureUtils;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;

import java.util.BitSet;
import java.util.ListIterator;

public class TerrainCarver implements Generator.Carvers {

    private final TerraChunkGenerator generator;

    public TerrainCarver(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void carveTerrain(BiomeManager biomes, IChunk chunk, GenerationStage.Carving type) {
        if (StructureUtils.hasOvergroundStructure(chunk)) {
            return;
        }

        chunk = new ChunkCarverFix(chunk, generator.getContext().materials);

        SharedSeedRandom random = new SharedSeedRandom();
        ChunkPos chunkpos = chunk.getPos();
        int chunkX = chunkpos.x;
        int chunkZ = chunkpos.z;
        BitSet mask = chunk.getCarvingMask(type);
        Biome biome = generator.getBiome(biomes, chunkpos.asBlockPos());

        for (int cx = chunkX - 8; cx <= chunkX + 8; ++cx) {
            for (int cz = chunkZ - 8; cz <= chunkZ + 8; ++cz) {
                ListIterator<ConfiguredCarver<?>> iterator = biome.getCarvers(type).listIterator();
                while (iterator.hasNext()) {
                    int index = iterator.nextIndex();
                    ConfiguredCarver<?> carver = iterator.next();
                    random.setLargeFeatureSeed(generator.getSeed() + index, cx, cz);
                    if (carver.shouldCarve(random, cx, cz)) {
                        carver.func_227207_a_(chunk, pos -> generator.getBiome(biomes, pos), random, generator.getSeaLevel(), cx, cz, chunkX, chunkZ, mask);
                    }
                }
            }
        }

    }
}
