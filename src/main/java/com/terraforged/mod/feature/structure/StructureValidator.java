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

package com.terraforged.mod.feature.structure;

import com.terraforged.mod.Log;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import com.terraforged.mod.chunk.settings.StructureSettings;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;

import java.util.*;
import java.util.function.Supplier;

public class StructureValidator {

    private static final String ERROR_MESSAGE = "Structure: {} has been added to {} biomes but has no separation"
            + " settings registered for it! We may not be able to generate/locate it :[";

    public static void validateConfigs(DimensionSettings dimension, TFBiomeContext context, StructureSettings settings) {
        final Map<Structure<?>, Set<Biome>> structures = new HashMap<>();
        final Biome[] overworldBiomes = BiomeAnalyser.getOverworldBiomes(context);

        for (Biome biome : overworldBiomes) {
            for (Supplier<StructureFeature<?, ?>> structureFeature : biome.getGenerationSettings().getStructures()) {
                Structure<?> structure = structureFeature.get().field_236268_b_;
                structures.computeIfAbsent(structure, s -> new HashSet<>()).add(biome);
            }
        }

        final DimensionStructuresSettings structuresSettings = dimension.getStructures();
        for (Map.Entry<Structure<?>, Set<Biome>> entry : structures.entrySet()) {
            if (structuresSettings.func_236197_a_(entry.getKey()) == null) {
                String name = Objects.toString(entry.getKey().getRegistryName());
                StructureSettings.StructureSeparation userSetting = settings.structures.get(name);

                if (userSetting != null && userSetting.disabled) {
                    continue;
                }

                Log.warn(ERROR_MESSAGE, name, entry.getValue().size());
            }
        }
    }
}
