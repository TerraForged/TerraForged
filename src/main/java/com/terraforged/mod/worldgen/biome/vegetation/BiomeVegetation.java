package com.terraforged.mod.worldgen.biome.vegetation;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class BiomeVegetation {
    public static BiomeVegetation NONE = new BiomeVegetation();
    public static final int STAGE = GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();

    protected static final Set<PlacementModifierType<?>> EXCLUSIONS = ImmutableSet.of(
            PlacementModifierType.BIOME_FILTER,
            PlacementModifierType.COUNT,
            PlacementModifierType.COUNT_ON_EVERY_LAYER,
            PlacementModifierType.NOISE_BASED_COUNT,
            PlacementModifierType.NOISE_THRESHOLD_COUNT
    );
    protected static final Set<PlacementModifierType<?>> TREE_EXCLUSIONS = ImmutableSet.<PlacementModifierType<?>>builder()
            .addAll(EXCLUSIONS)
            .add(PlacementModifierType.IN_SQUARE)
            .build();

    private final PlacedFeature[] trees;
    private final PlacedFeature[] grass;
    private final PlacedFeature[] other;

    private BiomeVegetation() {
        trees = new PlacedFeature[0];
        grass = new PlacedFeature[0];
        other = new PlacedFeature[0];
    }

    public BiomeVegetation(ResourceKey<Biome> key, RegistryAccess access) {
        var trees = new ArrayList<PlacedFeature>();
        var grass = new ArrayList<PlacedFeature>();
        var other = new ArrayList<PlacedFeature>();

        var biomeRegistry = access.registryOrThrow(Registry.BIOME_REGISTRY);
        var features = biomeRegistry.getOrThrow(key).getGenerationSettings().features();
        if (features.size() >= STAGE) {
            var vegetation = features.get(STAGE);
            var featureRegistry = access.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);

            for (var feature : vegetation) {
                var featureKey = featureRegistry.getKey(feature.get());
                if (featureKey == null) {
                    other.add(feature.get());
                } else {
                    if (featureKey.getPath().contains("tree")) {
                        trees.add(unwrap(feature, TREE_EXCLUSIONS));
                    } else if (featureKey.getPath().contains("grass")) {
                        grass.add(unwrap(feature, TREE_EXCLUSIONS));
                    } else {
                        other.add(unwrap(feature, EXCLUSIONS));
                    }
                }
            }
        }

        this.trees = trees.toArray(PlacedFeature[]::new);
        this.grass = grass.toArray(PlacedFeature[]::new);
        this.other = other.toArray(PlacedFeature[]::new);
    }

    public PlacedFeature[] getTrees() {
        return trees;
    }

    public PlacedFeature[] getGrass() {
        return grass;
    }

    public PlacedFeature[] getOther() {
        return other;
    }

    protected static PlacedFeature unwrap(Supplier<PlacedFeature> supplier, Set<PlacementModifierType<?>> exclusions) {
        try {
            PlacedFeature placed = supplier.get();

            var feature = getFeature(placed);
            var placements = new ArrayList<>(getPlacements(placed));
            placements.removeIf(placement -> exclusions.contains(placement.type()));

            return new PlacedFeature(feature, placements);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return supplier.get();
        }
    }

    @SuppressWarnings("unchecked")
    protected static Supplier<ConfiguredFeature<?, ?>> getFeature(PlacedFeature feature) throws IllegalAccessException {
        return (Supplier<ConfiguredFeature<?, ?>>) FEATURE_FIELD.get().get(feature);
    }

    @SuppressWarnings("unchecked")
    protected static List<PlacementModifier> getPlacements(PlacedFeature feature) throws IllegalAccessException {
        return (List<PlacementModifier>) PLACEMENT_FIELD.get().get(feature);
    }

    protected static final Supplier<Field> FEATURE_FIELD = Suppliers.memoize(() -> {
        for (var field : PlacedFeature.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == Supplier.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new Error("Oh no");
    });

    protected static final Supplier<Field> PLACEMENT_FIELD = Suppliers.memoize(() -> {
        for (var field : PlacedFeature.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == List.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new Error("Oh no");
    });
}
