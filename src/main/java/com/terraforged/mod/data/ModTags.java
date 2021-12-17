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

import com.terraforged.mod.registry.ModRegistries;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.asset.BiomeTag;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public interface ModTags extends ModRegistry {
    static void register() {
        var registry = BuiltinRegistries.BIOME;
        ModRegistries.register(BIOME_TAG, "trees/copses", Trees.copses(registry));
        ModRegistries.register(BIOME_TAG, "trees/hardy", Trees.hardy(registry));
        ModRegistries.register(BIOME_TAG, "trees/patchy", Trees.patchy(registry));
        ModRegistries.register(BIOME_TAG, "trees/sparse", Trees.sparse(registry));
        ModRegistries.register(BIOME_TAG, "trees/rainforest", Trees.rainforest(registry));
        ModRegistries.register(BIOME_TAG, "trees/sparse_rainforest", Trees.sparseRainforest(registry));
        ModRegistries.register(BIOME_TAG, "trees/temperate", Trees.temperate(registry));
    }

    class Trees {
        static BiomeTag copses(Registry<Biome> registry) {
            return BiomeTag.of(registry, Biomes.PLAINS, Biomes.MEADOW, Biomes.SNOWY_PLAINS, Biomes.SUNFLOWER_PLAINS);
        }

        static BiomeTag patchy(Registry<Biome> registry) {
            return BiomeTag.of(registry, Biomes.WOODED_BADLANDS, ModBiomes.OAK_FOREST.key());
        }

        static BiomeTag hardy(Registry<Biome> registry) {
            return BiomeTag.of(registry, Biomes.TAIGA, Biomes.DARK_FOREST, Biomes.WINDSWEPT_FOREST, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        }

        static BiomeTag sparse(Registry<Biome> registry) {
            return BiomeTag.of(registry, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA);
        }

        static BiomeTag rainforest(Registry<Biome> registry) {
            return BiomeTag.of(registry, Biomes.JUNGLE, Biomes.BAMBOO_JUNGLE, Biomes.SPARSE_JUNGLE);
        }

        static BiomeTag sparseRainforest(Registry<Biome> registry) {
            return BiomeTag.of(registry, Biomes.SPARSE_JUNGLE);
        }

        static BiomeTag temperate(Registry<Biome> registry) {
            return BiomeTag.of(registry, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.FLOWER_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST);
        }
    }
}
