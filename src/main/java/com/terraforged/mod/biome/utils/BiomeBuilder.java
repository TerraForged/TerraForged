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

package com.terraforged.mod.biome.utils;

import com.terraforged.mod.featuremanager.matcher.dynamic.DynamicMatcher;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
        } else if (BiomeDictionary.getTypes(key).contains(BiomeDictionary.Type.FOREST)) {
            BiomeManager.addBiome(type, new BiomeManager.BiomeEntry(key, 5));
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
        dictionaryTypes.retainAll(BiomeDictionary.getTypes(parentKey));

        settings = new BiomeGenerationSettings.Builder();
        features.clear();

        // surface
        settings.surfaceBuilder(parentBiome.getGenerationSettings().getSurfaceBuilder().get());
        // mobs
        mobSpawnSettings(parentBiome.getMobSettings());
        // category
        biomeCategory(parentBiome.getBiomeCategory());
        // structures
        parentBiome.getGenerationSettings().structures().forEach(s -> settings.addStructureStart(s.get()));
        // carvers
        for (GenerationStage.Carving stage : GenerationStage.Carving.values()) {
            List<Supplier<ConfiguredCarver<?>>> carvers = parentBiome.getGenerationSettings().getCarvers(stage);
            carvers.forEach(c -> this.settings.addCarver(stage, c.get()));
        }
        // features
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            List<Supplier<ConfiguredFeature<?, ?>>> features = parentBiome.getGenerationSettings().features().get(stage.ordinal());
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
        Biome biome = ForgeRegistries.BIOMES.getValue(key.location());
        if (biome == null) {
            return;
        }
        specialEffects(biome.getSpecialEffects());
    }

    public Biome getBiome() {
        return parentBiome;
    }

    public BiomeGenerationSettings.Builder getSettings() {
        return settings;
    }

    @Override
    public Biome build() {
        generationSettings(settings.build());
        return super.build();
    }

    public Biome build(RegistryKey<Biome> key) {
        features.forEach((stage, list) -> list.forEach(f -> getSettings().addFeature(stage, f.get())));
        generationSettings(settings.build());
        return super.build().setRegistryName(key.location());
    }
}
