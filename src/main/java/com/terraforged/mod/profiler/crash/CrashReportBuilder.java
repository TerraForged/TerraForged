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

package com.terraforged.mod.profiler.crash;

import com.google.gson.JsonElement;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.config.ConfigRef;
import com.terraforged.mod.profiler.ProfilerPrinter;
import com.terraforged.mod.util.DataUtils;
import com.terraforged.mod.util.reflect.Accessor;
import com.terraforged.mod.util.reflect.FieldAccessor;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.Structure;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CrashReportBuilder {

    private static final FieldAccessor<CrashReport, ?> ACCESSOR = Accessor.field(CrashReport.class, boolean.class);

    public static CrashReport buildCrashReport(IChunk chunk, TFChunkGenerator generator, Throwable t) {
        CrashReport report = new CrashReport(t.getMessage(), t.getCause());
        // prevents pollution of the report with duplicate stack-traces
        ACCESSOR.setUnchecked(report, false);
        try {
            addContext(chunk, generator, report);
        } catch (Throwable ctx) {
            report.addCategory("Reporting Exception").setDetail("Uh Oh", ctx);
        }
        return report;
    }

    private static void addContext(IChunk chunk, TFChunkGenerator generator, CrashReport report) {
        addContext(chunk, report.addCategory("Current Chunk"));
        addContext(generator, report.addCategory("TerraForged ChunkGenerator"));
        addContext(generator.getBiomeSource(), report.addCategory("TerraForged BiomeProvider"));
        addContext(ConfigManager.BIOME_WEIGHTS, report.addCategory("TerraForged Biome Weights Config"));
        addProfilerContext(report.addCategory("TerraForged Profiler"));
    }

    private static void addContext(IChunk chunk, CrashReportCategory report) {
        report.setDetail("Pos", chunk.getPos());
        report.setDetail("Status", chunk.getStatus().getName());
        report.setDetail("Structure Starts", getStructures(chunk.getAllStarts()));
        report.setDetail("Structure Refs", getStructures(chunk.getAllReferences()));
    }

    private static void addContext(TFChunkGenerator generator, CrashReportCategory report) {
        report.setDetail("Seed", generator.getSeed());
        report.setDetail("Settings", DataUtils.toJson(generator.getContext().terraSettings));
    }

    private static void addContext(TFBiomeProvider biomes, CrashReportCategory report) {
        report.setDetail("Overworld Biomes", biomes.possibleBiomes().stream().map(Biome::getRegistryName).collect(Collectors.toList()));
//        report.setDetail("Biome Map", biomes.getBiomeMap().toJson(biomes.getContext().gameContext));
    }

    private static void addContext(ConfigRef ref, CrashReportCategory report) {
        ref.forEach(report::setDetail);
    }

    private static void addProfilerContext(CrashReportCategory report) {
        StringWriter writer = new StringWriter();
        try {
            ProfilerPrinter.print(writer, "\t\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        report.setDetail("Timings", "\n" + writer.toString());
    }

    private static List<String> getStructures(Map<Structure<?>, ?> map) {
        return map.keySet().stream().map(CrashReportBuilder::getStructureName).sorted().collect(Collectors.toList());
    }

    private static String getStructureName(Structure<?> structure) {
        ResourceLocation name = structure.getRegistryName();
        if (name == null) {
            return structure.getFeatureName();
        }
        return name.toString();
    }

    private static String print(JsonElement jsonElement) {
        return "\n" + jsonElement.toString();
    }
}
