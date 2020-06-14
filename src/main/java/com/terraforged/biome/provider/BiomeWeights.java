package com.terraforged.biome.provider;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.terraforged.Log;
import com.terraforged.config.ConfigManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void readWeights() {
        CommentedConfig config = ConfigManager.BIOME_WEIGHTS.get();

        for (String key : config.valueMap().keySet()) {
            int weight = config.getInt(key);
            if (weight < 0) {
                continue;
            }

            ResourceLocation name = new ResourceLocation(key);
            if (!ForgeRegistries.BIOMES.containsKey(name)) {
                continue;
            }

            biomes.put(name, weight);
            Log.debug("Loaded custom biome weight: %s=%s", name, weight);
        }
    }
}
