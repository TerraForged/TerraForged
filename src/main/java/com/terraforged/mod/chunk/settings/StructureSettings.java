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

package com.terraforged.mod.chunk.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.terraforged.engine.serialization.annotation.Comment;
import com.terraforged.engine.serialization.annotation.Limit;
import com.terraforged.engine.serialization.annotation.Name;
import com.terraforged.engine.serialization.annotation.NoName;
import com.terraforged.engine.serialization.annotation.Rand;
import com.terraforged.engine.serialization.annotation.Range;
import com.terraforged.engine.serialization.annotation.Serializable;
import com.terraforged.engine.serialization.annotation.Sorted;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import com.terraforged.mod.structure.StructureUtils;
import com.terraforged.mod.structure.StructureValidator;
import com.terraforged.mod.util.DataUtils;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Serializable
public class StructureSettings {

    public StructureSpread stronghold = new StructureSpread();

    @NoName
    @Sorted
    public Map<String, StructureSeparation> structures = new LinkedHashMap<>();

    public boolean isDefault() {
        return structures.isEmpty();
    }

    public Map<String, StructureSeparation> getOrDefaultStructures() {
        if (isDefault()) {
            return StructureUtils.getOverworldStructureDefaults();
        }
        return structures;
    }

    public void load(DimensionStructuresSettings settings, TFBiomeContext context) {
        // Copy valid structure configs to a new instance
        StructureSettings defaults = new StructureSettings();

        JsonElement json = Codecs.encodeAndGet(DimensionStructuresSettings.CODEC, settings, JsonOps.INSTANCE);
        json = StructureUtils.addMissingStructures(json.getAsJsonObject());

        DataUtils.fromJson(json, defaults);

        // Remove structures that appear in biomes that don't generate in overworld
        StructureUtils.retainOverworldStructures(defaults.structures, settings, context);

        // Replace default values in the new instance with any contained on this instance
        this.structures.forEach((name, setting) -> defaults.structures.replace(name, setting));

        // Replace this structures map with the updated defaults
        this.structures = defaults.structures;
    }

    public DimensionStructuresSettings validateAndApply(TerraContext context, Supplier<DimensionSettings> settings) {
        DimensionSettings dimensionSettings = settings.get();
        StructureValidator.validateConfigs(dimensionSettings, context.biomeContext, this);
        return apply(dimensionSettings.structureSettings());
    }

    public DimensionStructuresSettings apply(DimensionStructuresSettings original) {
        return Codecs.modify(original, DimensionStructuresSettings.CODEC, root -> {
            root = StructureUtils.addMissingStructures(root.getAsJsonObject());

            if (isDefault()) {
                return root;
            }

            JsonObject dest = root.getAsJsonObject();
            JsonObject source = DataUtils.toJson(this).getAsJsonObject();

            // Replace stronghold settings user's preferences
            dest.add("stronghold", source.get("stronghold"));

            // Replace overworld structure settings with user's preferences
            JsonObject destStructures = dest.getAsJsonObject("structures");
            JsonObject sourceStructures = source.getAsJsonObject("structures");
            for (Map.Entry<String, JsonElement> entry : sourceStructures.entrySet()) {
                StructureSeparation settings = structures.get(entry.getKey());

                // Remove if user has disabled the structure
                if (settings != null && settings.disabled) {
                    destStructures.remove(entry.getKey());
                    continue;
                }

                // Add the user configuration
                destStructures.add(entry.getKey(), entry.getValue());
            }

            return root;
        });
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

        @Rand
        @Comment("A seed offset used to randomise stronghold placement")
        public int salt = 0;

        @Comment({
                "Set whether strongholds should only start in valid stronghold biomes. The vanilla behaviour",
                "(false) tries its best to move them to a valid biome but does not prevent generation if there",
                "are no valid biomes nearby. Setting to true outright blocks strongholds from starting in invalid",
                "biomes (note: it doesn't prevent them extending from a valid biome to an invalid one)."
        })
        public boolean constrainToBiomes = false;

        @Comment("Prevent this structure from generating.")
        public boolean disabled = false;
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

        @Comment("Prevent this structure from generating.")
        public boolean disabled = false;
    }
}
