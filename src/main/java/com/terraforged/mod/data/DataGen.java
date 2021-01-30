/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.mod.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;

import java.io.*;

public class DataGen {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void dumpData() {
        File dataDir = new File("data").getAbsoluteFile();
        TFBiomeContext context = new TFBiomeContext(DynamicRegistries.func_239770_b_());
        Biome[] biomes = BiomeAnalyser.getOverworldBiomes(context);
        WorldGenBiomes.genBiomeMap(dataDir, context);
        WorldGenBiomes.genBiomeData(dataDir, biomes, context);
        WorldGenBiomes.genBiomeWeights(dataDir, context);
        WorldGenBlocks.genBlockTags(dataDir);
        WorldGenFeatures.genBiomeFeatures(dataDir, biomes, context);
    }

    protected static void write(File file, IOConsumer consumer) {
        if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
            try (Writer writer = new BufferedWriter(new FileWriter(file))) {
                consumer.accept(writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static void write(JsonElement json, File dataDir, String path) {
        File file = new File(dataDir, path).getAbsoluteFile();
        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            return;
        }
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            write(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void write(JsonElement json, Writer writer) {
        GSON.toJson(json, writer);
    }

    protected static String getJsonPath(String type, ResourceLocation location) {
        if (location == null) {
            return "unknown";
        }
        return location.getNamespace() + "/" + type + "/" + location.getPath() + ".json";
    }

    public interface IOConsumer {

        void accept(Writer writer) throws IOException;
    }
}
