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

import com.google.gson.JsonObject;
import com.terraforged.engine.serialization.annotation.*;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import com.terraforged.mod.util.DataUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.gen.settings.StructureSpreadSettings;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Serializable
public class StructureSettings {

    public StructureSpread stronghold = new StructureSpread();

    @NoName
    @Sorted
    public Map<String, StructureSeparation> structures = new LinkedHashMap<>();


    public DimensionStructuresSettings apply(Supplier<DimensionSettings> settings) {
        init(settings);

        return Codecs.modify(settings.get().structureSettings(), DimensionStructuresSettings.CODEC, data -> {
            JsonObject structures = data.getAsJsonObject().getAsJsonObject("structures");
            if (structures == null) {
                structures = new JsonObject();
            }

            for (Map.Entry<String, StructureSeparation> entry : this.structures.entrySet()) {
                if (entry.getValue().disabled) {
                    structures.remove(entry.getKey());
                    continue;
                }
                structures.add(entry.getKey(), DataUtils.toJson(entry.getValue()));
            }

            return data;
        });
    }

    public StructureSettings init(Supplier<DimensionSettings> settings) {
        // Current settings held in this instance (loaded from level.dat) take priority

        // Then: add any missing structures from the datapack
        addSettings(settings.get().structureSettings());

        // Then: add any missing structures from the WorldGenRegistry
        addDefaults();

        return this;
    }

    // Adds settings contained in the DimensionStructuresSettings instance if they are missing
    // from the StructureSettings
    public StructureSettings addSettings(DimensionStructuresSettings structuresSettings) {
        initStronghold(structuresSettings.stronghold());
        initStructures(structuresSettings.structureConfig());
        return this;
    }

    // Adds settings contained in the default overworld DimensionStructuresSettings instance if
    // they are missing from the StructureSettings.
    public StructureSettings addDefaults() {
        DimensionSettings settings = WorldGenRegistries.NOISE_GENERATOR_SETTINGS.getOrThrow(DimensionSettings.OVERWORLD);
        DimensionStructuresSettings structuresSettings = settings.structureSettings();
        return addSettings(structuresSettings);
    }

    public void hideNonOverworld(TFBiomeContext context) {
        Predicate<String> predicate = getOverworldStructurePredicate(context);
        for (Map.Entry<String, StructureSeparation> entry : structures.entrySet()) {
            entry.getValue().hidden = !predicate.test(entry.getKey());
        }
    }

    private void initStronghold(@Nullable StructureSpreadSettings stronghold) {
        // Ignore if stronghold is already configured
        if (!this.stronghold.isDefaulted()) {
            return;
        }

        // Null or 0 count indicates to the chunk generator that strongholds are disabled
        this.stronghold.disabled = stronghold == null;

        if (stronghold == null) {
            return;
        }

        // Skip invalid configurations
        if (!validateStronghold(stronghold.count(), stronghold.spread(), stronghold.distance())) {
            Log.warn("Structure [minecraft:stronghold] configured with invalid settings. Ignoring to prevent crashes!");
            return;
        }

        // Copy settings
        this.stronghold.count = stronghold.count();
        this.stronghold.spread = stronghold.spread();
        this.stronghold.distance = stronghold.distance();
    }

    private void initStructures(Map<Structure<?>, StructureSeparationSettings> structures) {
        for (Map.Entry<Structure<?>, StructureSeparationSettings> entry : structures.entrySet()) {
            ResourceLocation registryName = entry.getKey().getRegistryName();
            StructureSeparationSettings settings = entry.getValue();

            // Ignore unregistered structures
            if (registryName == null) {
                continue;
            }

            String name = registryName.toString();
            StructureSettings.StructureSeparation separation = this.structures.get(name);

            // Create preferences for the missing structure
            if (separation == null) {
                separation = new StructureSettings.StructureSeparation();
                separation.disabled = false;
                separation.salt = settings.salt();
                separation.spacing = settings.spacing();
                separation.separation = settings.separation();
            }

            // Skip & log invalidate configuratioms
            if (!validateStructure(name, separation.spacing, separation.separation, separation.salt)) {
                Log.warn("Structure [{}] registered with invalid separation settings. Ignoring to prevent crashes!");
                continue;
            }

            // Only add missing entries so that user prefs aren't overwritten
            this.structures.putIfAbsent(name, separation);
        }
    }

    @Serializable
    public static class StructureSpread {
        private static final int DEF_DISTANCE = 32;
        private static final int DEF_SPREAD = 3;
        private static final int DEF_COUNT = 128;
        private static final int DEF_SALT = 0;

        @Range(min = 0, max = 1023)
        @Comment("Controls how far from spawn strongholds will start generating")
        public int distance = DEF_DISTANCE;

        @Range(min = 0, max = 1023)
        @Comment("Controls how spread apart strongholds will generate")
        public int spread = DEF_SPREAD;

        @Range(min = 1, max = 4095)
        @Comment("Controls the number of strongholds that will generate in the world")
        public int count = DEF_COUNT;

        @Rand
        @Comment("A seed offset used to randomise stronghold placement")
        public int salt = DEF_SALT;

        @Comment({
                "Set whether strongholds should only start in valid stronghold biomes. The vanilla behaviour",
                "(false) tries its best to move them to a valid biome but does not prevent generation if there",
                "are no valid biomes nearby. Setting to true outright blocks strongholds from starting in invalid",
                "biomes (note: it doesn't prevent them extending from a valid biome to an invalid one)."
        })
        public boolean constrainToBiomes = false;

        @Comment("Prevent this structure from generating.")
        public boolean disabled = false;

        public boolean isDefaulted() {
            return !disabled && !constrainToBiomes && distance == DEF_DISTANCE
                    && spread == DEF_SPREAD && count == DEF_COUNT && salt == DEF_SALT;
        }
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

        @Hide
        public transient boolean hidden = false;
    }

    private static Predicate<String> getOverworldStructurePredicate(TFBiomeContext context) {
        Biome[] biomes = BiomeAnalyser.getOverworldBiomes(context);
        Set<String> set = new HashSet<>();
        for (Biome biome : biomes) {
            biome.getGenerationSettings().structures().forEach(s -> {
                Structure<?> structure = s.get().feature;
                ResourceLocation name = structure.getRegistryName();
                if (name != null) {
                    set.add(name.toString());
                }
            });
        }
        return set::contains;
    }

    private static boolean validateStructure(String name, int spacing, int separation, int salt) {
        if (spacing < separation) {
            Log.err("[{}] structure spacing ({}) cannot be lower than separation ({})", name, spacing, separation);
            return false;
        }

        boolean result;
        result = checkRange(0, 4096, spacing, name, "spacing");
        result &= checkRange(0, 4096, separation, name, "separation");
        result &= checkRange(0, Integer.MAX_VALUE, salt, name, "salt");

        return result;
    }

    private static boolean validateStronghold(int count, int spread, int distance) {
        boolean result;
        result = checkRange(0, 1023, distance, "minecraft:stronghold", "distance");
        result &= checkRange(0, 1023, spread, "minecraft:stronghold", "spread");
        result &= checkRange(1, 4095, count, "minecraft:stronghold", "count");
        return result;
    }

    private static boolean checkRange(int min, int max, int value, String name, String variable) {
        if (value >= min && value <= max) {
            return true;
        }
        Log.err("[{}] structure setting: {} = {} is outside of the valid range: {} - {}", name, variable, value, min, max);
        return false;
    }
}
