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

package com.terraforged.mod.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import com.terraforged.mod.chunk.settings.StructureSettings;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.gen.settings.StructureSpreadSettings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class StructureUtils {
    private static final String ERROR_MESSAGE = "Critical error loading structure settings for [{}] (it may be configured incorrectly!): {}";

    public static JsonObject encode(DimensionStructuresSettings settings) {
        JsonObject root = new JsonObject();

        JsonElement stronghold = encodeStronghold(settings.stronghold());
        if (stronghold.isJsonNull()) {
            stronghold = encodeStronghold(DimensionStructuresSettings.DEFAULT_STRONGHOLD);
        }

        JsonObject structures = new JsonObject();
        for (Map.Entry<Structure<?>, StructureSeparationSettings> entry : settings.structureConfig().entrySet()) {
            String name = Objects.toString(entry.getKey().getRegistryName());
            JsonElement structure = encodeStructure(name, entry.getValue());
            if (!structure.isJsonNull()) {
                structures.add(name, structure);
            }
        }

        root.add("stronghold", stronghold);
        root.add("structures", structures);

        return root;
    }

    public static JsonElement encodeStronghold(StructureSpreadSettings settings) {
        try {
            return Codecs.encode(StructureSpreadSettings.CODEC, settings);
        } catch (Throwable t) {
            Log.err(ERROR_MESSAGE, "minecraft:stronghold", t);
            return JsonNull.INSTANCE;
        }
    }

    public static JsonElement encodeStructure(String name, StructureSeparationSettings settings) {
        try {
            return Codecs.encode(StructureSeparationSettings.CODEC, settings);
        } catch (Throwable t) {
            Log.err(ERROR_MESSAGE, name, t);
            return JsonNull.INSTANCE;
        }
    }

    public static void addMissingStructures(JsonObject dest) {
        DimensionSettings settings = WorldGenRegistries.NOISE_GENERATOR_SETTINGS.getOrThrow(DimensionSettings.OVERWORLD);
        JsonElement element = Codecs.encode(DimensionStructuresSettings.CODEC, settings.structureSettings());
        if (element.isJsonObject()) {
            JsonObject defaults = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
                if (!dest.has(entry.getKey())) {
                    dest.add(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public static Map<String, StructureSettings.StructureSeparation> getOverworldStructureDefaults() {
        Map<String, StructureSettings.StructureSeparation> map = new LinkedHashMap<>();

        DimensionSettings dimensionSettings = WorldGenRegistries.NOISE_GENERATOR_SETTINGS.getOrThrow(DimensionSettings.OVERWORLD);
        DimensionStructuresSettings structuresSettings = dimensionSettings.structureSettings();

        for (Map.Entry<Structure<?>, StructureSeparationSettings> entry : structuresSettings.structureConfig().entrySet()) {
            if (entry.getKey().getRegistryName() == null) {
                continue;
            }

            StructureSettings.StructureSeparation separation = new StructureSettings.StructureSeparation();
            separation.salt = entry.getValue().salt();
            separation.spacing = entry.getValue().spacing();
            separation.separation = entry.getValue().separation();

            map.put(Objects.toString(entry.getKey().getRegistryName()), separation);
        }

        return map;
    }

    public static void retainOverworldStructures(Map<String, ?> map, DimensionStructuresSettings settings, TFBiomeContext context) {
        Biome[] overworldBiomes = BiomeAnalyser.getOverworldBiomes(context);
        for (Structure<?> structure : settings.structureConfig().keySet()) {
            if (structure.getRegistryName() == null) {
                continue;
            }

            // Remove if stronghold (has its own settings) or isn't an overworld structure
            if (structure == Structure.STRONGHOLD || !hasStructure(structure, overworldBiomes)) {
                map.remove(structure.getRegistryName().toString());
            }
        }
    }

    public static boolean hasStructure(Structure<?> structure, Biome[] biomes) {
        for (Biome biome : biomes) {
            if (biome.getGenerationSettings().isValidStart(structure)) {
                return true;
            }
        }
        return false;
    }
}
