/*
 *
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
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.provider.BiomeHelper;
import com.terraforged.mod.biome.provider.BiomeWeights;
import com.terraforged.world.biome.BiomeType;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class WorldGenBiomes extends DataGen {

    public static void genBiomeMap(File dataDir) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            BiomeMap map = BiomeHelper.createBiomeMap();
            try (Writer writer = new BufferedWriter(new FileWriter(new File(dataDir, "biome_map.json")))) {
                new GsonBuilder().setPrettyPrinting().create().toJson(map.toJson(), writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void genBiomeWeights(File dataDir) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            BiomeWeights weights = new BiomeWeights();
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
        return new ResourceLocation("terraforged", type.name().toLowerCase());
    }
}
