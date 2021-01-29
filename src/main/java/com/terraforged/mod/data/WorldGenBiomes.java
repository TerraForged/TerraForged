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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.BiomeWeights;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.stream.IntStream;

public class WorldGenBiomes extends DataGen {

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

    public static void genBiomeWeights(File dataDir, TFBiomeContext context) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            BiomeWeights weights = new BiomeWeights(context);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dataDir, "biome_weights.txt")))) {
                writer.write("# REGISTERED BIOME WEIGHTS\n");
                weights.forEachEntry((name, weight) -> {
                    try {
                        writer.write(name + "=" + weight + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                writer.write("\n");
                writer.write("# UNREGISTERED BIOME WEIGHTS\n");
                weights.forEachUnregistered((name, weight) -> {
                    try {
                        writer.write(name + "=" + weight + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ResourceLocation getName(BiomeType type) {
        return new ResourceLocation(TerraForgedMod.MODID, type.name().toLowerCase());
    }
}
