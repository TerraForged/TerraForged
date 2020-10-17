package com.terraforged.mod.biome.utils;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.settings.StructureSeparationSettings;

public class StructureLocator {

    public static BlockPos findStructure(TerraChunkGenerator generator, IWorld world, StructureManager manager, Structure<?> structure, BlockPos center, int attempts, boolean first, StructureSeparationSettings settings) {
        long seed = generator.getSeed();
        int separation = settings.func_236668_a_();
        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;

        SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
        TerraBiomeProvider biomeProvider = generator.getBiomeProvider();

        try (Resource<Cell> cell = Cell.pooled()) {
            for (int radius = 0; radius <= attempts; ++radius) {
                for (int dx = -radius; dx <= radius; ++dx) {
                    boolean flag = dx == -radius || dx == radius;

                    for (int dz = -radius; dz <= radius; ++dz) {
                        boolean flag1 = dz == -radius || dz == radius;
                        if (flag || flag1) {
                            int x = chunkX + separation * dx;
                            int z = chunkZ + separation * dz;

                            Biome biome = biomeProvider.lookupBiome(cell.get(), x, z);
                            if (!biome.func_242440_e().func_242493_a(structure)) {
                                continue;
                            }

                            ChunkPos chunkpos = structure.func_236392_a_(settings, seed, sharedseedrandom, x, z);
                            IChunk ichunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart<?> start = manager.func_235013_a_(SectionPos.from(ichunk.getPos(), 0), structure, ichunk);
                            if (start != null && start.isValid()) {
                                if (first && start.isRefCountBelowMax()) {
                                    start.incrementRefCount();
                                    return start.getPos();
                                }

                                if (!first) {
                                    return start.getPos();
                                }
                            }

                            if (radius == 0) {
                                break;
                            }
                        }
                    }

                    if (radius == 0) {
                        break;
                    }
                }
            }
        }
        return null;
    }
}
