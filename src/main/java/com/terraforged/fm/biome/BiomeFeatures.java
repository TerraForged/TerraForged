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

package com.terraforged.fm.biome;

import com.google.common.base.Suppliers;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.structure.Structure;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BiomeFeatures {

    public static final BiomeFeatures NONE = new BiomeFeatures();

    private static final List<List<BiomeFeature>> NO_FEATURES = Stream.of(GenerationStage.Decoration.values())
            .map(stage -> Collections.<BiomeFeature>emptyList())
            .collect(Collectors.toList());

    private static final List<List<Structure<?>>> NO_STRUCTURES = Stream.of(GenerationStage.Decoration.values())
            .map(stage -> Collections.<Structure<?>>emptyList())
            .collect(Collectors.toList());

    private static final Supplier<List<List<Structure<?>>>> STRUCTURES = Suppliers.memoize(() -> {
        Map<GenerationStage.Decoration, List<Structure<?>>> map = Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy(Structure::func_236396_f_));
        List<List<Structure<?>>> list = new ArrayList<>();
        for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
            list.add(map.getOrDefault(stage, Collections.emptyList()));
        }
        return list;
    });

    private final List<List<BiomeFeature>> features;
    private final List<List<Structure<?>>> structures;

    private BiomeFeatures() {
        features = NO_FEATURES;
        structures = NO_STRUCTURES;
    }

    public BiomeFeatures(Builder builder) {
        this.features = builder.compileFeatures();
        this.structures = NO_STRUCTURES;
    }

    public List<List<BiomeFeature>> getFeatures() {
        return features;
    }

    public List<List<Structure<?>>> getStructures() {
        return structures;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int size;
        private Map<GenerationStage.Decoration, List<BiomeFeature>> features = Collections.emptyMap();

        public Builder add(GenerationStage.Decoration stage, Collection<BiomeFeature> features) {
            for (BiomeFeature feature : features) {
                add(stage, feature);
            }
            return this;
        }

        public Builder add(GenerationStage.Decoration stage, BiomeFeature feature) {
            if (features.isEmpty()) {
                features = new EnumMap<>(GenerationStage.Decoration.class);
            }
            features.computeIfAbsent(stage, s -> new ArrayList<>()).add(feature);
            size++;
            return this;
        }

        private List<List<BiomeFeature>> compileFeatures() {
            List<List<BiomeFeature>> list = new ArrayList<>(size);
            for (GenerationStage.Decoration stage : GenerationStage.Decoration.values()) {
                list.add(features.getOrDefault(stage, Collections.emptyList()));
            }
            return list;
        }

        public BiomeFeatures build() {
            return new BiomeFeatures(this);
        }
    }
}
