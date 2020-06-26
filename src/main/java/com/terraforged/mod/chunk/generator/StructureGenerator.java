package com.terraforged.mod.chunk.generator;

import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.fm.predicate.FeaturePredicate;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.Map;

public class StructureGenerator implements Generator.Structures {

    private final TerraChunkGenerator generator;

    public StructureGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void generateStructureStarts(BiomeManager biomes, IChunk chunk, TemplateManager templates) {
        ChunkPos chunkpos = chunk.getPos();
        generator.queueChunk(chunkpos.x, chunkpos.z);

        BlockPos biomePos = new BlockPos(chunkpos.getXStart() + 9, 0, chunkpos.getZStart() + 9);
        Biome biome = biomes.getBiome(biomePos);

        for (Structure<?> structure : Feature.STRUCTURES.values()) {
            if (generator.getBiomeProvider().hasStructure(structure)) {
                FeaturePredicate predicate = generator.getStructureManager().getPredicate(structure);
                if (!predicate.test(chunk, biome)) {
                    continue;
                }

                StructureStart existingStart = chunk.getStructureStart(structure.getStructureName());
                int refCount = existingStart != null ? existingStart.func_227457_j_() : 0;

                SharedSeedRandom random = new SharedSeedRandom();
                StructureStart start = StructureStart.DUMMY;

                if (structure.func_225558_a_(biomes, generator, random, chunkpos.x, chunkpos.z, biome)) {
                    StructureStart altStart = structure.getStartFactory().create(structure, chunkpos.x, chunkpos.z, MutableBoundingBox.getNewBoundingBox(), refCount, generator.getSeed());
                    altStart.init(generator, templates, chunkpos.x, chunkpos.z, biome);
                    start = altStart.isValid() ? altStart : StructureStart.DUMMY;
                }

                chunk.putStructureStart(structure.getStructureName(), start);
            }
        }
    }

    public void generateStructureReferences(IWorld world, IChunk chunk) {
        try {
            int radius = 8;

            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;

            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            int maxX = minX + 15;
            int maxZ = minZ + 15;

            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    int cx = chunkX + dx;
                    int cz = chunkZ + dz;
                    long chunkSeed = ChunkPos.asLong(cx, cz);
                    IChunk c = world.getChunk(cx, cz);
                    for (Map.Entry<String, StructureStart> entry : c.getStructureStarts().entrySet()) {
                        StructureStart start = entry.getValue();
                        if (start != StructureStart.DUMMY && start.getBoundingBox().intersectsWith(minX, minZ, maxX, maxZ)) {
                            chunk.addStructureReference(entry.getKey(), chunkSeed);
                            DebugPacketSender.sendStructureStart(world, start);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
