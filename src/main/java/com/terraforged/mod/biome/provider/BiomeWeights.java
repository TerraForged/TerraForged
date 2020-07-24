package com.terraforged.mod.biome.provider;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.terraforged.mod.Log;
import com.terraforged.mod.config.ConfigManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class BiomeWeights {

    private final int standardWeight;
    private final int forestWeight;
    private final int rareWeight;
    private final Map<ResourceLocation, Integer> biomes = new HashMap<>();

    public BiomeWeights() {
        this(10, 5, 1);
    }

    public BiomeWeights(int standard, int forest, int rare) {
        this.standardWeight = standard;
        this.forestWeight = forest;
        this.rareWeight = rare;

        // Get biome weights from Forge
        for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values()) {
            List<BiomeManager.BiomeEntry> entries = BiomeManager.getBiomes(type);
            if (entries == null) {
                continue;
            }
            for (BiomeManager.BiomeEntry entry : entries) {
                biomes.put(entry.biome.getRegistryName(), entry.itemWeight);
            }
        }

        // TF config gets final say
        readWeights();
    }

    public int getWeight(Biome biome) {
        Integer value = biomes.get(biome.getRegistryName());
        if (value != null) {
            return value;
        }
        if (BiomeDictionary.getTypes(biome).contains(BiomeDictionary.Type.RARE)) {
            return rareWeight;
        }
        if (biome.getCategory() == Biome.Category.FOREST) {
            return forestWeight;
        }
        return standardWeight;
    }

    public void forEachEntry(BiConsumer<ResourceLocation, Integer> consumer) {
        biomes.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                .forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }

    public void forEachUnregistered(BiConsumer<ResourceLocation, Integer> consumer) {
        for (Biome biome : ForgeRegistries.BIOMES) {
            if (!biomes.containsKey(biome.getRegistryName())) {
                int weight = getWeight(biome);
                consumer.accept(biome.getRegistryName(), weight);
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
            if (!ForgeRegistries.BIOMES.containsKey(name)) {
                if (!key.equals("terraforged:example_biome")) {
                    Log.err("Invalid biome defined: {}", name);
                }
                continue;
            }

            biomes.put(name, weight);
            Log.debug("Loaded custom biome weight: {}={}", name, weight);
        }
    }
}
