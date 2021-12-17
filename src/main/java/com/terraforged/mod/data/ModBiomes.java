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

import com.terraforged.mod.platform.Platform;
import com.terraforged.mod.registry.registrar.Registrar;
import com.terraforged.mod.worldgen.biome.biomes.ModBiome;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

public interface ModBiomes {
    ModBiome CAVE = ModBiome.of("cave", Biomes.DRIPSTONE_CAVES, builder -> {
        var genSettings = new BiomeGenerationSettings.Builder();
        genSettings.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.LARGE_DRIPSTONE);
        genSettings.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.POINTED_DRIPSTONE);
        genSettings.build();
        builder.generationSettings(genSettings.build());
    });

    ModBiome OAK_FOREST = ModBiome.of("oak_forest", Biomes.PLAINS, builder -> {});

    static void register() {
        register(Platform.ACTIVE_PLATFORM.get().getRegistrar(Registry.BIOME_REGISTRY));
    }

    static void register(Registrar<Biome> registrar) {
        registrar.register(CAVE.key(), CAVE.create());
        registrar.register(OAK_FOREST.key(), OAK_FOREST.create());
    }
}
