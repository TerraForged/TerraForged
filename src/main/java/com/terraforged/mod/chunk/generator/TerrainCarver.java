package com.terraforged.mod.chunk.generator;

import com.terraforged.core.cell.Cell;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.fix.ChunkCarverFix;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;

import java.util.BitSet;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class TerrainCarver implements Generator.Carvers {

    private final TerraChunkGenerator generator;

    public TerrainCarver(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void carveTerrain(BiomeManager biomes, IChunk chunk, GenerationStage.Carving type) {
        ChunkCarverFix carverChunk = new ChunkCarverFix(chunk, generator.getContext().materials);

        SharedSeedRandom random = new SharedSeedRandom();
        ChunkPos chunkpos = carverChunk.getPos();
        int chunkX = chunkpos.x;
        int chunkZ = chunkpos.z;

        int seaLevel = generator.getSeaLevel();
        BiomeLookup lookup = new BiomeLookup();
        BitSet mask = carverChunk.getCarvingMask(type);
        Biome biome = generator.getBiomeProvider().getBiome(chunkpos.getXStart(), chunkpos.getZStart());
        BiomeGenerationSettings settings = biome.func_242440_e();

        ListIterator<Supplier<ConfiguredCarver<?>>> iterator = settings.func_242489_a(type).listIterator();

        for (int cx = chunkX - 8; cx <= chunkX + 8; ++cx) {
            for (int cz = chunkZ - 8; cz <= chunkZ + 8; ++cz) {
                while (iterator.hasNext()) {
                    int index = iterator.nextIndex();
                    ConfiguredCarver<?> carver = iterator.next().get();
                    random.setLargeFeatureSeed(generator.getSeed() + index, cx, cz);
                    if (carver.shouldCarve(random, cx, cz)) {
                        carver.carveRegion(carverChunk, lookup, random, seaLevel, cx, cz, chunkX, chunkZ, mask);
                    }
                }

                // rewind
                while (iterator.hasPrevious()) {
                    iterator.previous();
                }
            }
        }
    }

    private class BiomeLookup implements Function<BlockPos, Biome> {

        private final Cell cell = new Cell();

        @Override
        public Biome apply(BlockPos pos) {
            return generator.getBiomeProvider().getBiome(cell, pos.getX(), pos.getZ());
        }
    }
}
