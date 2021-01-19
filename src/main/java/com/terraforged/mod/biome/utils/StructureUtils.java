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

package com.terraforged.mod.biome.utils;

import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;

import java.util.Map;

public class StructureUtils {

    public static void retainOverworldStructures(Map<String, ?> map, DimensionStructuresSettings settings, TFBiomeContext context) {
        Biome[] overworldBiomes = BiomeAnalyser.getOverworldBiomes(context);
        for (Structure<?> structure : settings.func_236195_a_().keySet()) {
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
            if (biome.getGenerationSettings().hasStructure(structure)) {
                return true;
            }
        }
        return false;
    }
}
