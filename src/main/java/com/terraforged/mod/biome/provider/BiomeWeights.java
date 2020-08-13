package com.terraforged.mod.biome.provider;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.Log;
import com.terraforged.mod.config.ConfigManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class BiomeWeights {

    private final int standardWeight;
    private final int forestWeight;
    private final int rareWeight;
    private final GameContext context;
    private final Map<ResourceLocation, Integer> biomes = new HashMap<>();

    public BiomeWeights(GameContext context) {
        this(10, 5, 1, context);
    }

    public BiomeWeights(int standard, int forest, int rare, GameContext context) {
        this.standardWeight = standard;
        this.forestWeight = forest;
        this.rareWeight = rare;
        this.context = context;

        // Get biome weights from Forge
//        for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values()) {
//            List<BiomeManager.BiomeEntry> entries = BiomeManager.getBiomes(type);
//            if (entries == null) {
//                continue;
//            }
//            for (BiomeManager.BiomeEntry entry : entries) {
//                biomes.put(entry.biome.getRegistryName(), entry.itemWeight);
//            }
//        }

        // TF config gets final say
        readWeights();
    }

    public int getWeight(Biome biome) {
        Integer value = biomes.get(context.biomes.getRegistryName(biome));
        if (value != null) {
            return value;
        }
//        if (BiomeDictionary.getTypes(biome).contains(BiomeDictionary.Type.RARE)) {
//            return rareWeight;
//        }
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
        for (Biome biome : context.biomes) {
            ResourceLocation name = context.biomes.getRegistryName(biome);
            if (!biomes.containsKey(name)) {
                int weight = getWeight(biome);
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

            biomes.put(name, weight);
            Log.debug("Loaded custom biome weight: {}={}", name, weight);
        }
    }
}
