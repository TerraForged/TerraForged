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

package com.terraforged.mod.worldgen.biome.vegetation;

import com.google.common.collect.ImmutableSet;
import com.terraforged.mod.util.ReflectionUtil;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VegetationFeatures {
    public static VegetationFeatures NONE = new VegetationFeatures();
    public static final int STAGE = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();

    private static final MethodHandle FEATURE_GETTER = ReflectionUtil.field(PlacedFeature.class, Holder.class);
    private static final MethodHandle PLACEMENTS_GETTER = ReflectionUtil.field(PlacedFeature.class, List.class);

    private static final Set<PlacementModifierType<?>> BIOME_CHECK = Set.of(PlacementModifierType.BIOME_FILTER);

    private static final Set<PlacementModifierType<?>> EXCLUSIONS = Set.of(
            PlacementModifierType.BIOME_FILTER,
            PlacementModifierType.COUNT,
            PlacementModifierType.COUNT_ON_EVERY_LAYER,
            PlacementModifierType.NOISE_BASED_COUNT,
            PlacementModifierType.NOISE_THRESHOLD_COUNT);

    private static final Set<PlacementModifierType<?>> TREE_EXCLUSIONS = ImmutableSet.<PlacementModifierType<?>>builder()
            .addAll(EXCLUSIONS)
            .add(PlacementModifierType.IN_SQUARE)
            .build();

    protected static final String[] TREE_KEYWORDS = {"tree", "spruce", "oak", "birch", "pine", "dark_forest_vegetation"};
    protected static final String[] GRASS_KEYWORDS = {"grass"};

    private final PlacedFeature[] trees;
    private final PlacedFeature[] grass;
    private final PlacedFeature[] other;

    private VegetationFeatures() {
        trees = new PlacedFeature[0];
        grass = new PlacedFeature[0];
        other = new PlacedFeature[0];
    }

    public VegetationFeatures(List<PlacedFeature> trees, List<PlacedFeature> grass, List<PlacedFeature> other) {
        this.trees = trees.toArray(PlacedFeature[]::new);
        this.grass = grass.toArray(PlacedFeature[]::new);
        this.other = other.toArray(PlacedFeature[]::new);
    }

    public PlacedFeature[] trees() {
        return trees;
    }

    public PlacedFeature[] grass() {
        return grass;
    }

    public PlacedFeature[] other() {
        return other;
    }

    public static VegetationFeatures create(Biome biome, RegistryAccess access, VegetationConfig config) {
        var trees = new ArrayList<PlacedFeature>();
        var grass = new ArrayList<PlacedFeature>();
        var other = new ArrayList<PlacedFeature>();
        boolean custom = config != VegetationConfig.NONE;

        var features = biome.getGenerationSettings().features();
        if (features.size() > STAGE) {
            var vegetation = features.get(STAGE);
            var featureRegistry = access.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);

            for (var feature : vegetation) {
                var featureKey = featureRegistry.getKey(feature.value());
                if (featureKey == null) {
                    other.add(feature.value());
                } else {
                    var path = featureKey.getPath();
                    if (matches(path, TREE_KEYWORDS)) {
                        trees.add(unwrap(feature, TREE_EXCLUSIONS, custom));
                    } else if (matches(path, GRASS_KEYWORDS)) {
                        grass.add(feature.value());
                    } else {
                        other.add(feature.value());
                    }
                }
            }
        }

        return new VegetationFeatures(trees, grass, other);
    }

    protected static boolean matches(String name, String[] keywords) {
        for (var keyword : keywords) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static PlacedFeature unwrap(Holder<PlacedFeature> supplier, Set<PlacementModifierType<?>> exclusions, boolean custom) {
        if (!custom) return supplier.value();

        try {
            PlacedFeature placed = supplier.value();

            var feature = getFeature(placed);
            var placements = new ArrayList<>(getPlacements(placed));
            placements.removeIf(placement -> exclusions.contains(placement.type()));

            return new PlacedFeature(feature, placements);
        } catch (Throwable t) {
            t.printStackTrace();
            return supplier.value();
        }
    }

    @SuppressWarnings("unchecked")
    protected static Holder<ConfiguredFeature<?, ?>> getFeature(PlacedFeature feature) throws Throwable {
        return (Holder<ConfiguredFeature<?, ?>>) FEATURE_GETTER.invokeExact(feature);
    }

    @SuppressWarnings("unchecked")
    protected static List<PlacementModifier> getPlacements(PlacedFeature feature) throws Throwable {
        return (List<PlacementModifier>) PLACEMENTS_GETTER.invokeExact(feature);
    }
}
