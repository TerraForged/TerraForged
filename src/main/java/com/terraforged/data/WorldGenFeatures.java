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

package com.terraforged.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;

public class WorldGenFeatures extends DataGen {

    public static void genBiomeFeatures(File dataDir) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            for (Biome biome : ForgeRegistries.BIOMES) {
                genBiomeFeatures(dataDir, biome);
            }
        }
    }

    private static void genBiomeFeatures(File dir, Biome biome) {
        write(new File(dir, getJsonPath("features", biome.getRegistryName())), writer -> {
            JsonObject root = new JsonObject();
            for (GenerationStage.Decoration type : GenerationStage.Decoration.values()) {
                JsonArray features = new JsonArray();
                for (ConfiguredFeature<?, ?> feature : biome.getFeatures(type)) {
                    try {
                        Dynamic<JsonElement> dynamic = feature.serialize(JsonOps.INSTANCE);
                        features.add(dynamic.getValue());
                    } catch (NullPointerException e) {
                        new NullPointerException("Badly written feature: " + feature.feature.getRegistryName()).printStackTrace();
                    }
                }
                root.add(type.getName(), features);
            }
            write(root, writer);
        });
    }
}
