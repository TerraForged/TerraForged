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

package com.terraforged.mod.chunk.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.terraforged.engine.serialization.annotation.*;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.utils.StructureUtils;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import com.terraforged.mod.util.DataUtils;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;

import java.util.LinkedHashMap;
import java.util.Map;

@Serializable
public class StructureSettings {

    public StructureSpread stronghold = new StructureSpread();

    @Sorted
    public Map<String, StructureSeparation> structures = new LinkedHashMap<>();

    public boolean isDefault() {
        return structures.isEmpty();
    }

    public void read(DimensionStructuresSettings settings, TFBiomeContext context) {
        JsonElement json = Codecs.encodeAndGet(DimensionStructuresSettings.field_236190_a_, settings, JsonOps.INSTANCE);
        DataUtils.fromJson(json, this);

        StructureUtils.retainOverworldStructures(structures, settings, context);
    }

    public DimensionStructuresSettings write(DimensionStructuresSettings original) {
        if (isDefault()) {
            return original;
        } else {
            return Codecs.modify(original, DimensionStructuresSettings.field_236190_a_, root -> {
                JsonObject dest = root.getAsJsonObject();
                JsonObject source = DataUtils.toJson(this).getAsJsonObject();

                // Replace stronghold settings with our own
                dest.add("stronghold", source.get("stronghold"));

                // Replace overworld structure settings with our own
                JsonObject destStructures = dest.getAsJsonObject("structures");
                JsonObject sourceStructures = source.getAsJsonObject("structures");
                for (Map.Entry<String, JsonElement> entry : sourceStructures.entrySet()) {
                    destStructures.add(entry.getKey(), entry.getValue());
                }

                return root;
            });
        }
    }

    @Serializable
    public static class StructureSpread {

        @Range(min = 0, max = 1023)
        @Comment("Controls how far from spawn strongholds will start generating")
        public int distance = 32;

        @Range(min = 0, max = 1023)
        @Comment("Controls how spread apart strongholds will generate")
        public int spread = 3;

        @Range(min = 1, max = 4095)
        @Comment("Controls the number of strongholds that will generate in the world")
        public int count = 128;
    }

    @Serializable
    public static class StructureSeparation {

        @Limit(lower = "separation", pad = 1)
        @Range(min = 1, max = 1000)
        @Comment({
                "Controls the size of the grid used to generate the structure.",
                "Structures will attempt to generate once per grid cell. Larger",
                "spacing values will make the structure appear less frequently."
        })
        public int spacing = 1;

        @Limit(upper = "spacing", pad = 1)
        @Range(min = 0, max = 1000)
        @Comment({
                "Controls the minimum distance between instances of the structure.",
                "Larger values guarantee larger distances between structures of the",
                "same type but cause them to generate more 'grid-aligned'.",
                "Lower values will produce a more random distribution but may allow",
                "instances to generate closer together."
        })
        public int separation = 0;

        @Rand
        @Name("seed")
        @Range(min = 0, max = Integer.MAX_VALUE)
        @Comment("A random seed value for the structure.")
        public int salt = 0;
    }
}
