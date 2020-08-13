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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.fm.FeatureSerializer;
import com.terraforged.fm.GameContext;
import com.terraforged.fm.util.FeatureDebugger;
import com.terraforged.mod.Log;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

public class WorldGenFeatures extends DataGen {

    public static void genBiomeFeatures(File dataDir, GameContext context) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            for (Biome biome : context.biomes) {
                genBiomeFeatures(dataDir, biome, context);
            }
        }
    }

    private static void genBiomeFeatures(File dir, Biome biome, GameContext context) {
        write(new File(dir, getJsonPath("features", context.biomes.getRegistryName(biome))), writer -> {
            JsonObject root = new JsonObject();
            List<List<Supplier<ConfiguredFeature<?, ?>>>> stageFeatures = biome.func_242440_e().func_242498_c();

            for (GenerationStage.Decoration type : GenerationStage.Decoration.values()) {
                JsonArray features = new JsonArray();
                for (Supplier<ConfiguredFeature<?, ?>> feature : stageFeatures.get(type.ordinal())) {
                    try {
                        JsonElement element = FeatureSerializer.serialize(feature.get());
                        features.add(element);
                    } catch (Throwable t) {
                        String name = context.biomes.getName(biome) + "";
                        List<String> errors = FeatureDebugger.getErrors(feature.get());
                        Log.debug("Unable to serialize feature in biome: {}", name);
                        if (errors.isEmpty()) {
                            Log.debug("Unable to determine issues. See stacktrace:", t);
                        } else {
                            for (String error : errors) {
                                Log.debug(" - {}", error);
                            }
                        }
                    }
                }
                root.add(type.name(), features);
            }
            write(root, writer);
        });
    }
}
