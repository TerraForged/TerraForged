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

package com.terraforged.mod.biome.provider.analyser;

import com.terraforged.engine.world.biome.BiomeData;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.biome.map.BiomeMapBuilder;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.provider.BiomeHelper;
import com.terraforged.mod.biome.provider.BiomeWeights;
import com.terraforged.mod.biome.provider.TFBiomeContext;
import com.terraforged.mod.featuremanager.GameContext;
import com.terraforged.noise.util.Vec2f;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.*;
import java.util.function.Function;

public class BiomeAnalyser {

    private static final Map<BiomeType, BiomePredicate> PREDICATES = new HashMap<BiomeType, BiomePredicate>() {{
        put(BiomeType.TROPICAL_RAINFOREST, BiomePredicate.TROPICAL_RAINFOREST);
        put(BiomeType.SAVANNA, BiomePredicate.SAVANNA.or(BiomePredicate.MESA).not(BiomePredicate.DESERT).not(BiomePredicate.STEPPE).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN).not(BiomePredicate.WETLAND));
        put(BiomeType.DESERT, BiomePredicate.DESERT.or(BiomePredicate.MESA).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN).not(BiomePredicate.WETLAND));
        put(BiomeType.TEMPERATE_RAINFOREST, BiomePredicate.TEMPERATE_RAINFOREST.not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.TEMPERATE_FOREST, BiomePredicate.TEMPERATE_FOREST.not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN).not(BiomePredicate.WETLAND));
        put(BiomeType.GRASSLAND, BiomePredicate.GRASSLAND.not(BiomePredicate.WETLAND).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.COLD_STEPPE, BiomePredicate.COLD_STEPPE.not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.STEPPE, BiomePredicate.STEPPE.not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.TAIGA, BiomePredicate.TAIGA.not(BiomePredicate.TUNDRA).not(BiomePredicate.COLD_STEPPE).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.TUNDRA, BiomePredicate.TUNDRA.not(BiomePredicate.TAIGA).not(BiomePredicate.BEACH).not(BiomePredicate.MOUNTAIN));
        put(BiomeType.ALPINE, BiomePredicate.MOUNTAIN);
    }};

    public static BiomeMap createBiomeMap(GameContext context) {
        List<BiomeData> biomes = getAllBiomeData(context);
        BiomeWeights weights = new BiomeWeights(context);

        TFBiomeContext biomeContext = new TFBiomeContext(context);
        BiomeMap.Builder<RegistryKey<Biome>> builder = BiomeMapBuilder.create(biomeContext);
        for (BiomeData data : biomes) {
            Biome biome = (Biome) data.reference;
            RegistryKey<Biome> key = context.biomes.getKey(biome);
            int weight = weights.getWeight(biome);
            if (BiomePredicate.BEACH.test(data)) {
                builder.addBeach(key, weight);
            } else if (BiomePredicate.COAST.test(data)) {
                builder.addCoast(key, weight);
            } else if (BiomePredicate.OCEAN.test(data)) {
                builder.addOcean(key, weight);
            } else if (BiomePredicate.RIVER.test(data)) {
                builder.addRiver(key, weight);
            } else if (BiomePredicate.LAKE.test(data)) {
                builder.addLake(key, weight);
            } else if (BiomePredicate.WETLAND.test(data)) {
                builder.addWetland(key, weight);
            } else if (BiomePredicate.VOLCANO.test(data)) {
                builder.addVolcano(key, weight);
            } else if (BiomePredicate.MOUNTAIN.test(data)) {
                builder.addMountain(key, weight);
            } else {
                Collection<BiomeType> types = getTypes(data, biome);
                for (BiomeType type : types) {
                    // shouldn't happen
                    if (type == BiomeType.ALPINE) {
                        continue;
                    }
                    builder.addLand(type, key, weight);
                }
            }
        }

        builder.addLand(BiomeType.TEMPERATE_RAINFOREST, Biomes.PLAINS, 5);
        builder.addLand(BiomeType.TEMPERATE_FOREST, Biomes.FLOWER_FOREST, 2);
        builder.addLand(BiomeType.TEMPERATE_FOREST, Biomes.PLAINS, 5);
        builder.addLand(BiomeType.TUNDRA, ModBiomes.SNOWY_TAIGA_SCRUB, 2);
        builder.addLand(BiomeType.TAIGA, ModBiomes.TAIGA_SCRUB, 2);

        return builder.build();
    }

    public static List<Biome> getOverworldBiomes(GameContext context) {
        List<BiomeData> biomes = getAllBiomeData(context);
        Set<Biome> result = new HashSet<>(biomes.size());
        for (BiomeData data : biomes) {
            Biome biome = (Biome) data.reference;
            if (BiomePredicate.BEACH.test(data)) {
                result.add(biome);
            } else if (BiomePredicate.COAST.test(data)) {
                result.add(biome);
            } else if (BiomePredicate.OCEAN.test(data)) {
                result.add(biome);
            } else if (BiomePredicate.RIVER.test(data)) {
                result.add(biome);
            } else if (BiomePredicate.LAKE.test(data)) {
                result.add(biome);
            } else if (BiomePredicate.WETLAND.test(data)) {
                result.add(biome);
            } else if (BiomePredicate.VOLCANO.test(data)) {
                result.add(biome);
            } else if (BiomePredicate.MOUNTAIN.test(data)) {
                result.add(biome);
            } else if (getTypes(data, biome).size() > 0) {
                result.add(biome);
            }
        }
        return new ArrayList<>(result);
    }

    public static Collection<BiomeType> getTypes(BiomeData data, Biome biome) {
        Set<BiomeType> types = new HashSet<>();
        for (Map.Entry<BiomeType, BiomePredicate> entry : PREDICATES.entrySet()) {
            if (entry.getValue().test(data, biome)) {
                types.add(entry.getKey());
            }
        }
        return types;
    }

    public static List<BiomeData> getAllBiomeData(GameContext context) {
        Collection<Biome> biomes = getAllOverworldBiomes(context);
        Vec2f tempRange = getRange(biomes, BiomeHelper::getDefaultTemperature);
        Vec2f moistRange = getRange(biomes, Biome::getDownfall);
        List<BiomeData> list = new LinkedList<>();
        for (Biome biome : biomes) {
            String name = context.biomes.getName(biome);
            float moisture = (biome.getDownfall() - moistRange.x) / (moistRange.y - moistRange.x);
            float temperature = (BiomeHelper.getDefaultTemperature(biome) - tempRange.x) / (tempRange.y - tempRange.x);
            int color = BiomeHelper.getSurface(biome).getTop().getMaterial().getColor().colorValue;
            list.add(new BiomeData(name, biome, color, moisture, temperature));
        }
        return list;
    }

    private static List<Biome> getAllOverworldBiomes(GameContext context) {
        List<Biome> biomes = new ArrayList<>(200);
        for (Biome biome : context.biomes) {
            if (filter(biome, context)) {
                continue;
            }
            biomes.add(biome);
        }
        return biomes;
    }

    private static boolean filter(Biome biome, GameContext context) {
        if (biome.getCategory() == Biome.Category.NONE) {
            return true;
        }
        if (biome.getCategory() == Biome.Category.THEEND) {
            return true;
        }
        if (biome.getCategory() == Biome.Category.NETHER) {
            return true;
        }
        if (biome == context.biomes.get(Biomes.MUSHROOM_FIELD_SHORE) || biome == context.biomes.get(Biomes.MOUNTAIN_EDGE)) {
            return true;
        }
        if (context.biomes.getName(biome).contains("hills")) {
            return true;
        }
        // exclude non-overworld biomes
        return !BiomeHelper.isOverworldBiome(biome, context);
    }

    private static Vec2f getRange(Collection<Biome> biomes, Function<Biome, Float> getter) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (Biome biome : biomes) {
            float value = getter.apply(biome);
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        return new Vec2f(min, max);
    }
}
