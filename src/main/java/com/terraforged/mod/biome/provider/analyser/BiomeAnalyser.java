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

package com.terraforged.mod.biome.provider.analyser;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.biome.map.BiomeMapBuilder;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.BiomeHelper;
import com.terraforged.mod.biome.provider.BiomeWeights;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

public class BiomeAnalyser {
    private static final Map<BiomeType, BiomePredicate> PREDICATES = new HashMap<BiomeType, BiomePredicate>() {{
        put(BiomeType.TROPICAL_RAINFOREST, BiomePredicate.TROPICAL_RAINFOREST);
        put(BiomeType.SAVANNA, BiomePredicate.SAVANNA.or(BiomePredicate.MESA).not(BiomePredicate.DESERT).not(BiomePredicate.STEPPE).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN).not(BiomePredicate.WETLAND));
        put(BiomeType.DESERT, BiomePredicate.DESERT.or(BiomePredicate.MESA).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN).not(BiomePredicate.WETLAND));
        put(BiomeType.TEMPERATE_RAINFOREST, BiomePredicate.TEMPERATE_RAINFOREST.not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.TEMPERATE_FOREST, BiomePredicate.TEMPERATE_FOREST.not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN).not(BiomePredicate.WETLAND));
        put(BiomeType.GRASSLAND, BiomePredicate.GRASSLAND.not(BiomePredicate.WETLAND).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.COLD_STEPPE, BiomePredicate.COLD_STEPPE.not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.STEPPE, BiomePredicate.STEPPE.not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN).not(BiomePredicate.COLD_STEPPE));
        put(BiomeType.TAIGA, BiomePredicate.TAIGA.not(BiomePredicate.TUNDRA).not(BiomePredicate.COLD_STEPPE).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.TUNDRA, BiomePredicate.TUNDRA.not(BiomePredicate.TAIGA).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.ALPINE, BiomePredicate.MOUNTAIN);
    }};

    private static final BiomeFilter[] EXCLUSION_FILTERS = {
            (biome, context) -> biome.getBiomeCategory() == Biome.Category.THEEND,
            (biome, context) -> biome.getBiomeCategory() == Biome.Category.NETHER,
            (biome, context) -> context.biomes.getName(biome).contains("hills"),
            (biome, context) -> biome == context.biomes.get(Biomes.THE_VOID),
            (biome, context) -> biome == context.biomes.get(Biomes.MOUNTAIN_EDGE),
            (biome, context) -> !BiomeHelper.isOverworldBiome(biome, context),
    };

    public static BiomeMap<RegistryKey<Biome>> createBiomeMap(TFBiomeContext context) {
        BiomeMap.Builder<RegistryKey<Biome>> builder = BiomeMapBuilder.create(context);
        BiomeWeights weights = new BiomeWeights(context);
        collectOverworldBiomes(context, weights, builder);
        builder.addLand(BiomeType.TEMPERATE_RAINFOREST, Biomes.PLAINS, 5);
        builder.addLand(BiomeType.TEMPERATE_FOREST, Biomes.FLOWER_FOREST, 2);
        builder.addLand(BiomeType.TEMPERATE_FOREST, Biomes.PLAINS, 5);
        builder.addLand(BiomeType.TUNDRA, ModBiomes.SNOWY_TAIGA_SCRUB, 2);
        builder.addLand(BiomeType.TAIGA, ModBiomes.TAIGA_SCRUB, 2);
        return builder.build();
    }

    public static Biome[] getOverworldBiomes(TFBiomeContext context) {
        TFBiomeCollector collector = new TFBiomeCollector(context);
        collectOverworldBiomes(context, i -> 0, collector);
        return collector.getBiomes();
    }

    public static List<Biome> getOverworldBiomesList(TFBiomeContext context) {
        return getOverworldBiomesList(context, Function.identity());
    }

    public static <T> List<T> getOverworldBiomesList(TFBiomeContext context, Function<Biome, T> mapper) {
        return getOverworldBiomes(context, mapper, ImmutableList.builder()).build();
    }

    public static Set<Biome> getOverworldBiomesSet(TFBiomeContext context) {
        return getOverworldBiomesSet(context, Function.identity());
    }

    public static <T> Set<T> getOverworldBiomesSet(TFBiomeContext context, Function<Biome, T> mapper) {
        return getOverworldBiomes(context, mapper, ImmutableSet.builder()).build();
    }

    public static <T, C extends ImmutableCollection.Builder<T>> C getOverworldBiomes(TFBiomeContext context, Function<Biome, T> mapper, C builder) {
        Biome[] biomes = getOverworldBiomes(context);
        for (Biome biome : biomes) {
            T t = mapper.apply(biome);
            builder.add(t);
        }
        return builder;
    }

    private static void collectOverworldBiomes(TFBiomeContext context, IntUnaryOperator weightFunc, BiomeMap.Builder<RegistryKey<Biome>> builder) {
        for (Biome biome : context.biomes) {
            if (filter(biome, context)) {
                continue;
            }

            int id = context.biomes.getId(biome);
            RegistryKey<Biome> key = context.biomes.getKey(biome);
            int weight = weightFunc.applyAsInt(id);
            if (BiomePredicate.BEACH.test(id, context)) {
                builder.addBeach(key, weight);
            } else if (BiomePredicate.COAST.test(id, context)) {
                builder.addCoast(key, weight);
            } else if (BiomePredicate.OCEAN.test(id, context)) {
                builder.addOcean(key, weight);
            } else if (BiomePredicate.RIVER.test(id, context)) {
                builder.addRiver(key, weight);
            } else if (BiomePredicate.LAKE.test(id, context)) {
                builder.addLake(key, weight);
            } else if (BiomePredicate.WETLAND.test(id, context)) {
                builder.addWetland(key, weight);
            } else if (BiomePredicate.VOLCANO.test(id, context)) {
                builder.addVolcano(key, weight);
            } else if (BiomePredicate.MOUNTAIN.test(id, context)) {
                builder.addMountain(key, weight);
            } else {
                Collection<BiomeType> types = getTypes(id, context);
                for (BiomeType type : types) {
                    // shouldn't happen
                    if (type == BiomeType.ALPINE) {
                        continue;
                    }
                    builder.addLand(type, key, weight);
                }
            }
        }
    }

    public static Collection<BiomeType> getTypes(int biome, TFBiomeContext context) {
        Set<BiomeType> types = new HashSet<>();
        for (Map.Entry<BiomeType, BiomePredicate> entry : PREDICATES.entrySet()) {
            if (entry.getValue().test(biome, context)) {
                types.add(entry.getKey());
            }
        }
        return types;
    }

    private static boolean filter(Biome biome, TFBiomeContext context) {
        for (BiomeFilter filter : EXCLUSION_FILTERS) {
            if (filter.test(biome, context)) {
                return true;
            }
        }
        return false;
    }
}
