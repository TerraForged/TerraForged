package com.terraforged.mod.biome.utils;

import com.terraforged.fm.matcher.dynamic.DynamicMatcher;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
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
    private final Map<GenerationStage.Decoration, List<Supplier<ConfiguredFeature<?, ?>>>> features = new EnumMap<>(GenerationStage.Decoration.class);

    private RegistryKey<Biome> parentKey;
    private BiomeGenerationSettings.Builder settings = new BiomeGenerationSettings.Builder();

    private int weight = -1;
    private BiomeManager.BiomeType type = null;
    private final List<BiomeDictionary.Type> dictionaryTypes = new ArrayList<>();

    public BiomeBuilder(RegistryKey<Biome> key, Biome biome) {
        this.parentBiome = biome;
        this.parentKey = key;
        this.dictionaryTypes.addAll(BiomeDictionary.getTypes(parentKey));
    }

    public void setParentKey(RegistryKey<Biome> parentKey) {
        this.parentKey = parentKey;
    }

    public RegistryKey<Biome> getParentKey() {
        return parentKey;
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
        settings.withSurfaceBuilder(parentBiome.getGenerationSettings().getSurfaceBuilder().get());
        // mobs
        withMobSpawnSettings(parentBiome.getMobSpawnInfo());
        // category
        category(parentBiome.getCategory());
        // structures
        parentBiome.getGenerationSettings().getStructures().forEach(s -> settings.withStructure(s.get()));
        // features
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            List<Supplier<ConfiguredFeature<?, ?>>> features = parentBiome.getGenerationSettings().getFeatures().get(stage.ordinal());
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
        Biome biome = ForgeRegistries.BIOMES.getValue(key.getLocation());
        if (biome == null) {
            return;
        }
        setEffects(biome.getAmbience());
    }

    public Biome getBiome() {
        return parentBiome;
    }

    public BiomeGenerationSettings.Builder getSettings() {
        return settings;
    }

    public Biome build() {
        withGenerationSettings(settings.build());
        return super.build();
    }

    public Biome build(RegistryKey<Biome> key) {
        features.forEach((stage, list) -> list.forEach(f -> getSettings().withFeature(stage, f.get())));
        withGenerationSettings(settings.build());
        return super.build().setRegistryName(key.getLocation());
    }
}
