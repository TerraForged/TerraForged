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

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.key.EntryKey;
import com.terraforged.mod.worldgen.biome.biomes.ModBiome;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

import static com.terraforged.mod.TerraForged.BIOMES;

public interface ModBiomes {
    EntryKey<Biome> CAVE = TerraForged.BIOMES.entryKey("cave");
    EntryKey<Biome> OAK_FOREST = TerraForged.BIOMES.entryKey("oak_forest");

    static void register() {
        TerraForged.register(BIOMES, "cave", ModBiome.create(Biomes.DRIPSTONE_CAVES, builder -> {
            var genSettings = new BiomeGenerationSettings.Builder();
            genSettings.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.LARGE_DRIPSTONE);
            genSettings.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.POINTED_DRIPSTONE);
            genSettings.build();
            builder.generationSettings(genSettings.build());
        }));

        TerraForged.register(BIOMES, "oak_forest", ModBiome.create(Biomes.PLAINS, builder -> {
//        builder.biomeCategory(Biome.BiomeCategory.FOREST);
        }));
    }
}
