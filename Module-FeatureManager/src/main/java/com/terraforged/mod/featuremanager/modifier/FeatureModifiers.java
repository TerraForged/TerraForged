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

package com.terraforged.mod.featuremanager.modifier;

import com.google.gson.JsonElement;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.featuremanager.FeatureManager;
import com.terraforged.mod.featuremanager.FeatureSerializer;
import com.terraforged.mod.featuremanager.biome.BiomeFeature;
import com.terraforged.mod.featuremanager.predicate.BiomePredicate;
import com.terraforged.mod.featuremanager.predicate.FeaturePredicate;
import com.terraforged.mod.featuremanager.transformer.*;
import com.terraforged.mod.featuremanager.util.FeatureDebugger;
import com.terraforged.mod.featuremanager.util.identity.FeatureIdentifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureModifiers extends Event {

    private final ModifierList<FeatureReplacer> replacers = new ModifierList<>();
    private final ModifierList<FeatureInjector> injectors = new ModifierList<>();
    private final ModifierList<FeatureAppender> appenders = new ModifierList<>();
    private final ModifierList<FeatureTransformer> transformers = new ModifierList<>();
    private final ModifierList<FeaturePredicate> predicates = new ModifierList<>();

    private final TFBiomeContext context;

    public TFBiomeContext getContext() {
        return context;
    }

    public FeatureModifiers(TFBiomeContext context) {
        this.context = context;
    }

    public ModifierList<FeatureReplacer> getReplacers() {
        return replacers;
    }

    public ModifierList<FeatureInjector> getInjectors() {
        return injectors;
    }

    public ModifierList<FeatureAppender> getAppenders() {
        return appenders;
    }

    public ModifierList<FeaturePredicate> getPredicates() {
        return predicates;
    }

    public ModifierList<FeatureTransformer> getTransformers() {
        return transformers;
    }

    public void sort() {
        replacers.sort();
        predicates.sort();
        transformers.sort();
    }

    public List<BiomeFeature> getAppenders(GenerationStage.Decoration stage, Biome biome, InjectionPosition position) {
        return getAppenders(stage, position, biome);
    }

    public ModifierSet getFeature(GenerationStage.Decoration stage, Biome biome, ConfiguredFeature<?, ?> feature) {
        try {
            JsonElement element = FeatureSerializer.serialize(feature);
            ConfiguredFeature<?, ?> result = getFeature(biome, feature, element);
            if (result != feature) {
                // re-serialize if feature has been changed
                element = FeatureSerializer.serialize(result);
            }

            FeaturePredicate predicate = getPredicate(biome, element);
            List<BiomeFeature> before = getInjectors(biome, predicate, element, InjectionPosition.BEFORE);
            List<BiomeFeature> after = getInjectors(biome, predicate, element, InjectionPosition.AFTER);

            FeatureIdentifier identity = FeatureIdentifier.getIdentity(feature, element);
            return new ModifierSet(new BiomeFeature(predicate, result, identity), before, after);
        } catch (Throwable t) {
            String name = context.biomes.getName(biome);
            List<String> errors = FeatureDebugger.getErrors(feature);
            FeatureManager.LOG.debug(FeatureSerializer.MARKER, "Unable to serialize feature in biome: {}", name);
            if (errors.isEmpty()) {
                FeatureManager.LOG.debug("Unable to determine issues. See stacktrace:", t);
            } else {
                for (String error : errors) {
                    FeatureManager.LOG.debug(FeatureSerializer.MARKER, " - {}", error);
                }
            }
        }
        return new ModifierSet(new BiomeFeature(FeaturePredicate.ALLOW, feature, FeatureIdentifier.getIdentity(feature)));
    }

    private ConfiguredFeature<?, ?> getFeature(Biome biome, ConfiguredFeature<?, ?> feature, JsonElement element) {
        for (Modifier<FeatureReplacer> modifier : replacers) {
            if (modifier.getMatcher().test(biome, element)) {
                return modifier.getModifier().get();
            }
        }

        boolean modified = false;
        for (Modifier<FeatureTransformer> modifier : transformers) {
            if (modifier.getMatcher().test(biome, element)) {
                modified = true;
                element = modifier.getModifier().apply(element);
            }
        }

        if (!modified) {
            return feature;
        }

        try {
            return FeatureSerializer.deserializeUnchecked(element);
        } catch (Throwable t) {
            FeatureManager.LOG.warn(FeatureSerializer.MARKER, "Unable to deserialize biome feature: {}", biome);
            t.printStackTrace();
            return feature;
        }
    }

    private List<BiomeFeature> getInjectors(Biome biome, FeaturePredicate predicate, JsonElement element, InjectionPosition type) {
        List<BiomeFeature> result = Collections.emptyList();
        for (Modifier<FeatureInjector> modifier : getInjectors()) {
            if (modifier.getModifier().getPosition() != type) {
                continue;
            }
            if (modifier.getMatcher().test(biome, element)) {
                if (result.isEmpty()) {
                    result = new ArrayList<>();
                }
                ConfiguredFeature<?, ?> feature = modifier.getModifier().getFeature();
                FeatureIdentifier identity = FeatureIdentifier.getIdentity(feature);
                result.add(new BiomeFeature(predicate, feature, identity));
            }
        }
        return result;
    }

    private List<BiomeFeature> getAppenders(GenerationStage.Decoration stage, InjectionPosition position, Biome biome) {
        List<BiomeFeature> result = Collections.emptyList();
        for (Modifier<FeatureAppender> modifier : getAppenders()) {
            if (modifier.getModifier().getPosition() != position) {
                continue;
            }
            if (modifier.getModifier().getStage() != stage) {
                continue;
            }
            if (modifier.getMatcher().getBiomeMatcher().test(biome)) {
                if (result.isEmpty()) {
                    result = new ArrayList<>();
                }
                ConfiguredFeature<?, ?> feature = modifier.getModifier().getFeature();
                FeatureIdentifier identity = FeatureIdentifier.getIdentity(feature);
                result.add(new BiomeFeature(BiomePredicate.ALLOW, feature, identity));
            }
        }
        return result;
    }

    private FeaturePredicate getPredicate(Biome biome, JsonElement element) {
        for (Modifier<FeaturePredicate> modifier : predicates) {
            if (modifier.getMatcher().test(biome, element)) {
                return modifier.getModifier();
            }
        }
        return FeaturePredicate.ALLOW;
    }
}
