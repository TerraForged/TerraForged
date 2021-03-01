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

import com.terraforged.mod.Log;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import com.terraforged.mod.chunk.settings.StructureSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StructureValidator {

    private static final String REMOVED = "A third-party mod has removed structure [{}] from all overworld biomes so it cannot be generated!";
    private static final String UNREGISTERED = "Structure [{}] does not have any SeparationSettings registered for it so we cannot generated it!";

    public static void validateConfigs(DimensionSettings dimension, TFBiomeContext context, StructureSettings settings) {
        Log.info("Validating user structure preferences...");

        final DimensionStructuresSettings structuresSettings = dimension.getStructures();
        final List<Structure<?>> activeStructures = getActiveStructures(context);
        final Map<String, StructureSettings.StructureSeparation> userSettings = settings.getOrDefaultStructures();

        // Check for structures that have been removed from biomes when the user has it enabled
        for (Map.Entry<String, StructureSettings.StructureSeparation> entry : userSettings.entrySet()) {
            if (entry.getValue().disabled) {
                continue;
            }

            ResourceLocation name = ResourceLocation.tryCreate(entry.getKey());
            if (name == null) {
                continue;
            }

            Structure<?> structure = ForgeRegistries.STRUCTURE_FEATURES.getValue(name);
            if (structure != null && structuresSettings.func_236197_a_(structure) == null) {
                Log.info(REMOVED, name);
            }
        }

        // Check for mods removing strongholds from all biomes when the user has it enabled
        if (!settings.stronghold.disabled && !activeStructures.contains(Structure.STRONGHOLD)) {
            Log.info(REMOVED, Structure.STRONGHOLD.getRegistryName());
        }

        // Check for structures that have been added to biomes without having registered generation settings for it
        for (Structure<?> structure : activeStructures) {
            if (structuresSettings.func_236197_a_(structure) == null) {
                String name = Objects.toString(structure.getRegistryName());
                StructureSettings.StructureSeparation userSetting = userSettings.get(name);

                // Ignore if user has disabled it anyway
                if (userSetting != null && userSetting.disabled) {
                    continue;
                }

                Log.warn(UNREGISTERED, name);
            }
        }
    }

    private static List<Structure<?>> getActiveStructures(TFBiomeContext context) {
        final Set<Structure<?>> structures = new HashSet<>();
        final Biome[] overworldBiomes = BiomeAnalyser.getOverworldBiomes(context);

        for (Biome biome : overworldBiomes) {
            for (Supplier<StructureFeature<?, ?>> structureFeature : biome.getGenerationSettings().getStructures()) {
                Structure<?> structure = structureFeature.get().field_236268_b_;
                structures.add(structure);
            }
        }

        return structures.stream()
                .filter(structure -> structure.getRegistryName() != null)
                .sorted(Comparator.comparing(Structure::getRegistryName))
                .collect(Collectors.toList());
    }
}
