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

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.terraforged.mod.Log;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenSettingsExport;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldGenExport extends DataGen {

    public static void export(File dataDir, MinecraftServer server, Biome[] biomes) {
        DynamicRegistries registries = server.registryAccess();
        DynamicOps<JsonElement> ops = WorldGenSettingsExport.create(JsonOps.INSTANCE, registries);
        export(dataDir, setOf(biomes), registries, ops, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC);
        export(dataDir, registries, ops, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredCarver.DIRECT_CODEC);
        export(dataDir, registries, ops, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC);
        export(dataDir, registries, ops, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, StructureFeature.DIRECT_CODEC);
        export(dataDir, registries, ops, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DimensionSettings.DIRECT_CODEC);
    }

    protected static <T> Set<T> setOf(T[] elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    protected static <T> void export(File dataDir,
                                     DynamicRegistries registries,
                                     DynamicOps<JsonElement> ops,
                                     RegistryKey<Registry<T>> key,
                                     Codec<T> codec) {
        export(dataDir, Collections.emptySet(), registries, ops, key, codec);
    }

    protected static <T> void export(File dataDir,
                                     Set<?> filter,
                                     DynamicRegistries registries,
                                     DynamicOps<JsonElement> ops,
                                     RegistryKey<Registry<T>> key,
                                     Codec<T> codec) {
        Registry<T> registry = registries.registryOrThrow(key);

        for (Map.Entry<RegistryKey<T>, T> entry : registry.entrySet()) {
            if (!filter.isEmpty() && !filter.contains(entry.getValue())) {
                continue;
            }

            EncodeResult result = encode(ops, entry.getKey(), entry.getValue(), codec);
            if (result.json.isJsonNull()) {
                Log.err("Failed first encode: {}. Error: {}", entry.getKey(), result.error);
                result = encode(JsonOps.INSTANCE, entry.getKey(), entry.getValue(), codec);
            }

            if (result.json.isJsonNull()) {
                Log.err("Failed second encode: {}. Error: {}", entry.getKey(), result.error);
                continue;
            }

            write(result.json, dataDir, getJsonPath(entry.getKey()));
        }
    }

    protected static <T> EncodeResult encode(DynamicOps<JsonElement> ops, RegistryKey<T> key, T value, Codec<T> codec) {
        DataResult<JsonElement> result = codec.encodeStart(ops, value);
        JsonElement json = result.result().orElse(JsonNull.INSTANCE);
        String error = result.error().map(DataResult.PartialResult::message).orElse("unknown");
        return new EncodeResult(json, error);
    }

    protected static class EncodeResult {

        public final JsonElement json;
        public final String error;

        public EncodeResult(JsonElement json, String error) {
            this.json = json;
            this.error = error;
        }
    }
}
