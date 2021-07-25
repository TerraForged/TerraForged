/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.chunk.generator;

import com.terraforged.mod.chunk.TFChunkGenerator;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;

import java.util.function.Supplier;

public class StructureGenerator implements Generator.Structures {

    private final TFChunkGenerator generator;
    private final DimensionStructuresSettings structuresSettings;

    public StructureGenerator(TFChunkGenerator generator, DimensionStructuresSettings structuresSettings) {
        this.generator = generator;
        this.structuresSettings = structuresSettings;
    }

    @Override
    public StructureSeparationSettings getSeparationSettings(Structure<?> structure) {
        return structuresSettings.getConfig(structure);
    }

    @Override
    public void generateStructureStarts(IChunk chunk, DynamicRegistries registries, StructureManager structures, TemplateManager templates) {
        long seed = generator.getSeed();
        ChunkPos pos = chunk.getPos();
        generator.queueChunk(pos);

        int biomeX = chunkToBiomeChunkCenter(pos.x);
        int biomeZ = chunkToBiomeChunkCenter(pos.z);
        Biome biome = generator.getBiomeSource().getNoiseBiome(biomeX, 0, biomeZ);
        generate(chunk, pos, biome, StructureFeatures.STRONGHOLD, registries, structures, templates, seed);

        for (Supplier<StructureFeature<?, ?>> supplier : biome.getGenerationSettings().structures()) {
            this.generate(chunk, pos, biome, supplier.get(), registries, structures, templates, seed);
        }
    }

    private void generate(IChunk chunk, ChunkPos pos, Biome biome, StructureFeature<?, ?> structure, DynamicRegistries registries, StructureManager structures, TemplateManager templates, long seed) {
        SectionPos sectionpos = SectionPos.of(chunk.getPos(), 0);
        StructureStart<?> start = structures.getStartForFeature(sectionpos, structure.feature, chunk);
        int i = start != null ? start.getReferences() : 0;

        StructureSeparationSettings settings = getSeparationSettings(structure.feature);
        if (settings != null) {
            StructureStart<?> start1 = structure.generate(registries, generator, generator.getBiomeSource(), templates, seed, pos, biome, i, settings);
            structures.setStartForFeature(sectionpos, structure.feature, start1, chunk);
        }
    }

    @Override
    public void generateStructureReferences(ISeedReader world, IChunk chunk, StructureManager structures) {
        int radius = 8;
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        int endX = startX + 15;
        int endZ = startZ + 15;
        SectionPos sectionpos = SectionPos.of(chunk.getPos(), 0);

        for (int x = chunkX - radius; x <= chunkX + radius; ++x) {
            for (int z = chunkZ - radius; z <= chunkZ + radius; ++z) {
                long posId = ChunkPos.asLong(x, z);

                for (StructureStart<?> start : world.getChunk(x, z).getAllStarts().values()) {
                    try {
                        if (start != StructureStart.INVALID_START && start.getBoundingBox().intersects(startX, startZ, endX, endZ)) {
                            structures.addReferenceForFeature(sectionpos, start.getFeature(), posId, chunk);
                            DebugPacketSender.sendStructurePacket(world, start);
                        }
                    } catch (Exception exception) {
                        CrashReport crashreport = CrashReport.forThrowable(exception, "Generating structure reference");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Structure");
                        crashreportcategory.setDetail("Id", () -> start.getFeature().getRegistryName() + "");
                        crashreportcategory.setDetail("Name", () -> start.getFeature().getFeatureName());
                        crashreportcategory.setDetail("Class", () -> start.getFeature().getClass().getCanonicalName());
                        throw new ReportedException(crashreport);
                    }
                }
            }
        }
    }

    public static int chunkToBiomeChunkCenter(int chunk) {
        return (chunk << 2) + 2;
    }

    public static int blockToBiomeChunkCenter(int block) {
        return chunkToBiomeChunkCenter(block >> 4);
    }
}
