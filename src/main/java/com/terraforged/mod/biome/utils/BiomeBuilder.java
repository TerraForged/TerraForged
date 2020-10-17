package com.terraforged.mod.biome.utils;

import com.terraforged.fm.matcher.dynamic.DynamicMatcher;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;

public class BiomeBuilder extends Biome.Builder {

    private final Biome parentBiome;
    private final RegistryKey<Biome> parentKey;
    private final Map<GenerationStage.Decoration, List<Supplier<ConfiguredFeature<?, ?>>>> features = new EnumMap<>(GenerationStage.Decoration.class);
    private BiomeGenerationSettings.Builder settings = new BiomeGenerationSettings.Builder();

    private int weight = -1;
    private BiomeManager.BiomeType type = null;
    private final List<BiomeDictionary.Type> dictionaryTypes = new ArrayList<>();

    public BiomeBuilder(RegistryKey<Biome> key, Biome biome) {
        this.parentBiome = biome;
        this.parentKey = key;
        this.dictionaryTypes.addAll(BiomeDictionary.getTypes(parentKey));
    }

    public void registerTypes(RegistryKey<Biome> key) {
        dictionaryTypes.forEach(type -> BiomeDictionary.addTypes(key, type));
        BiomeDictionary.addTypes(key, BiomeDictionary.Type.OVERWORLD);
    }

    public void registerWeight(RegistryKey<Biome> key) {
        if (type == null) {
            return;
        }
        if (weight > 0) {
            BiomeManager.addBiome(type, new BiomeManager.BiomeEntry(key, weight));
        } else if (BiomeDictionary.getTypes(key).contains(BiomeDictionary.Type.RARE)) {
            BiomeManager.addBiome(type, new BiomeManager.BiomeEntry(key, 2));
        } else {
            BiomeManager.addBiome(type, new BiomeManager.BiomeEntry(key, 10));
        }
    }

    public void type(BiomeManager.BiomeType type) {
        this.type = type;
    }

    public void weight(int weight) {
        this.weight = weight;
    }

    public void setType(BiomeDictionary.Type... types) {
        dictionaryTypes.clear();
        addType(types);
    }

    public void addType(BiomeDictionary.Type... types) {
        Collections.addAll(dictionaryTypes, types);
    }

    protected BiomeBuilder init() {
        settings = new BiomeGenerationSettings.Builder();
        // surface
        settings.func_242517_a(parentBiome.func_242440_e().func_242500_d().get());
        // mobs
        func_242458_a(parentBiome.func_242433_b());
        // category
        category(parentBiome.getCategory());
        // structures
        parentBiome.func_242440_e().func_242487_a().forEach(s -> settings.func_242516_a(s.get()));
        // features
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            List<Supplier<ConfiguredFeature<?, ?>>> features = parentBiome.func_242440_e().func_242498_c().get(stage.ordinal());
            features.forEach(f -> this.features.computeIfAbsent(stage, s -> new ArrayList<>()).add(f));
        }
        return this;
    }

    public void filterFeatures(DynamicMatcher... filters) {
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            List<Supplier<ConfiguredFeature<?, ?>>> list = features.computeIfAbsent(stage, s -> new ArrayList<>());
            list.removeIf(f -> {
                ConfiguredFeature<?, ?> feature = f.get();
                for (DynamicMatcher matcher : filters) {
                    if (matcher.test(feature)) {
                        return true;
                    }
                }
                return false;
            });
        }
    }

    public void copyAmbience(RegistryKey<Biome> key) {
        Biome biome = ForgeRegistries.BIOMES.getValue(key.func_240901_a_());
        if (biome == null) {
            return;
        }
        func_235097_a_(biome.func_235089_q_());
    }

    public Biome getBiome() {
        return parentBiome;
    }

    public BiomeGenerationSettings.Builder getSettings() {
        return settings;
    }

    public Biome build() {
        func_242457_a(settings.func_242508_a());
        return func_242455_a();
    }

    public Biome build(RegistryKey<Biome> key) {
        features.forEach((stage, list) -> list.forEach(f -> getSettings().func_242513_a(stage, f.get())));
        func_242457_a(settings.func_242508_a());
        return func_242455_a().setRegistryName(key.func_240901_a_());
    }
}
