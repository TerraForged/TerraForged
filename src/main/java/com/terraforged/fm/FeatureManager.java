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

package com.terraforged.fm;

import com.terraforged.fm.biome.BiomeFeatures;
import com.terraforged.fm.data.DataManager;
import com.terraforged.fm.modifier.FeatureModifierLoader;
import com.terraforged.fm.modifier.FeatureModifiers;
import com.terraforged.fm.modifier.ModifierSet;
import com.terraforged.fm.template.TemplateManager;
import com.terraforged.fm.transformer.InjectionPosition;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FeatureManager implements FeatureDecorator {

    public static final Logger LOG = LogManager.getLogger("FeatureManager");
    public static final Marker INIT = MarkerManager.getMarker("INIT");

    private final Map<Biome, BiomeFeatures> biomes;

    public FeatureManager(Map<Biome, BiomeFeatures> biomes) {
        this.biomes = biomes;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return this;
    }

    public BiomeFeatures getFeatures(Biome biome) {
        return biomes.getOrDefault(biome, BiomeFeatures.NONE);
    }

    public static DataManager data(File dir) {
        return DataManager.of(dir);
    }

    public static FeatureModifiers modifiers(DataManager data, boolean load, GameContext context) {
        if (load) {
            return FeatureModifierLoader.load(data, context);
        }
        return new FeatureModifiers(context);
    }

    public static void initData(DataManager manager) {
        TemplateManager.getInstance().load(manager);
    }

    public static void clearData() {
        TemplateManager.getInstance().clear();
    }

    public static FeatureManager create(FeatureModifiers modifiers) {
        LOG.debug(INIT, "Initializing FeatureManager");
        int predicates = modifiers.getPredicates().size();
        int replacers = modifiers.getReplacers().size();
        int injectors = modifiers.getInjectors().size();
        int transformers = modifiers.getTransformers().size();
        LOG.debug(INIT, " Predicates: {}, Replacers: {}, Injectors: {}, Transformers: {}", predicates, replacers, injectors, transformers);

        modifiers.sort();

        LOG.debug(INIT, " Compiling biome feature lists");
        Map<Biome, BiomeFeatures> biomes = new HashMap<>();
        for (Biome biome : modifiers.getContext().biomes) {
            BiomeFeatures features;
            try {
                features = compute(biome, modifiers);
            } catch (Throwable t) {
                t.printStackTrace();
                features = BiomeFeatures.NONE;
            }
            biomes.put(biome, features);
        }

        LOG.debug(INIT, " Initialization complete");
        return new FeatureManager(biomes);
    }

    private static BiomeFeatures compute(Biome biome, FeatureModifiers modifiers) {
        BiomeFeatures.Builder builder = BiomeFeatures.builder();
        List<List<Supplier<ConfiguredFeature<?, ?>>>> features = biome.getGenerationSettings().getFeatures();
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            // add 'prepend' injectors to the head of the feature list
            builder.add(stage, modifiers.getAppenders(stage, biome, InjectionPosition.HEAD));

            if (stage.ordinal() >= features.size()) {
                break;
            }

            for (Supplier<ConfiguredFeature<?, ?>> feature : features.get(stage.ordinal())) {
                ModifierSet modifierSet = modifiers.getFeature(stage, biome, feature.get());
                builder.add(stage, modifierSet.before);
                builder.add(stage, modifierSet.feature);
                builder.add(stage, modifierSet.after);
            }

            // add 'append' injectors to the tail of the feature list
            builder.add(stage, modifiers.getAppenders(stage, biome, InjectionPosition.TAIL));
        }
        return builder.build();
    }
}
