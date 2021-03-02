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

package com.terraforged.mod.data;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.BiomeWeights;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.stream.IntStream;

public class WorldGenBiomes extends DataGen {

    private static final String FORGE_REG_NAME_KEY = "forge:registry_name";

    public static void genBiomeMap(File dataDir, TFBiomeContext context) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            BiomeMap<?> map = BiomeAnalyser.createBiomeMap(context);
            JsonObject rootJson = new JsonObject();

            map.forEach((terrain, biomeSet) -> {
                JsonObject biomeGroups = new JsonObject();
                rootJson.add(terrain, biomeGroups);

                biomeSet.forEach((type, biomes) -> {
                    JsonArray biomesArray = new JsonArray();
                    biomeGroups.add(type, biomesArray);

                    IntStream.of(biomes).distinct()
                            .mapToObj(map.getContext()::getName)
                            .sorted()
                            .forEach(biomesArray::add);
                });
            });

            try (Writer writer = new BufferedWriter(new FileWriter(new File(dataDir, "biome_map.json")))) {
                new GsonBuilder().setPrettyPrinting().create().toJson(rootJson, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void genBiomeData(File dataDir, Biome[] biomes, TFBiomeContext context) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            for (Biome biome : biomes) {
                try {
                    ResourceLocation name = context.biomes.getRegistryName(biome);
                    if (name == null) {
                        continue;
                    }

                    JsonElement json = Codecs.encodeAndGet(Biome.CODEC, biome, JsonOps.INSTANCE);

                    if (json.isJsonObject()) {
                        json.getAsJsonObject().remove(FORGE_REG_NAME_KEY);
                    }

                    write(json, dataDir, getJsonPath("worldgen/biome", name));
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }
    }

    public static void genBiomeWeights(File dataDir, TFBiomeContext context) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            BiomeWeights weights = new BiomeWeights(context);
            try (PrintWriter writer = newPrinter(dataDir, "biome_weights.conf")) {
                writer.println("# REGISTERED BIOME WEIGHTS");
                weights.forEachEntry((name, weight) -> write(name, weight, writer));

                writer.println("# UNREGISTERED BIOME WEIGHTS");
                weights.forEachUnregistered((name, weight) -> write(name, weight, writer));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static PrintWriter newPrinter(File dir, String filename) throws IOException {
        return new PrintWriter(new BufferedWriter(new FileWriter(new File(dir, filename))));
    }

    private static void write(String name, int weight, PrintWriter writer) {
        writer.print('"');
        writer.print(name);
        writer.print('"');
        writer.print(" = ");
        writer.println(weight);
    }
}
