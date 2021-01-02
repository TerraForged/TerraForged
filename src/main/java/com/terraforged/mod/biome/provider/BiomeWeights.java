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

package com.terraforged.mod.biome.provider;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.config.ConfigManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class BiomeWeights {

    private final int standardWeight;
    private final int forestWeight;
    private final int rareWeight;
    private final TFBiomeContext context;
    private final Map<String, Integer> biomes = new HashMap<>();

    public BiomeWeights(TFBiomeContext context) {
        this(10, 5, 2, context);
    }

    public BiomeWeights(int standard, int forest, int rare, TFBiomeContext context) {
        this.standardWeight = standard;
        this.forestWeight = forest;
        this.rareWeight = rare;
        this.context = context;

        // Get biome weights from Forge
        for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values()) {
            List<BiomeManager.BiomeEntry> entries = BiomeManager.getBiomes(type);
            if (entries == null) {
                continue;
            }
            for (BiomeManager.BiomeEntry entry : entries) {
                biomes.put(entry.getKey().getLocation().toString(), entry.itemWeight);
            }
        }

        // TF config gets final say
        readWeights();
    }

    public int getWeight(int biome) {
        String name = context.getName(biome);
        Integer value = biomes.get(name);
        if (value != null) {
            return value;
        }
        if (BiomeDictionary.getTypes(context.getValue(biome)).contains(BiomeDictionary.Type.RARE)) {
            return rareWeight;
        }
        if (context.biomes.get(biome).getCategory() == Biome.Category.FOREST) {
            return forestWeight;
        }
        return standardWeight;
    }

    public void forEachEntry(BiConsumer<String, Integer> consumer) {
        biomes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }

    public void forEachUnregistered(BiConsumer<String, Integer> consumer) {
        for (Biome biome : context.biomes) {
            int id = context.biomes.getId(biome);
            String name = context.getName(id);
            if (!biomes.containsKey(name)) {
                int weight = getWeight(id);
                consumer.accept(name, weight);
            }
        }
    }

    private void readWeights() {
        CommentedConfig config = ConfigManager.BIOME_WEIGHTS.get();

        for (String key : config.valueMap().keySet()) {
            int weight = config.getInt(key);
            if (weight < 0) {
                continue;
            }

            ResourceLocation name = new ResourceLocation(key);
            if (!context.biomes.contains(name)) {
                if (!key.equals("terraforged:example_biome")) {
                    Log.err("Invalid biome defined: {}", name);
                }
                continue;
            }

            biomes.put(key, weight);
            Log.debug("Loaded custom biome weight: {}={}", name, weight);
        }
    }
}
