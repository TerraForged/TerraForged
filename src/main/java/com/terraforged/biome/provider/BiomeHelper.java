/*
 *
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

package com.terraforged.biome.provider;

import com.terraforged.biome.ModBiomes;
import com.terraforged.biome.map.BiomeMap;
import com.terraforged.biome.map.BiomeMapBuilder;
import com.terraforged.biome.map.BiomePredicate;
import com.terraforged.n2d.util.Vec2f;
import com.terraforged.world.biome.BiomeData;
import com.terraforged.world.biome.BiomeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.OceanRuinConfig;
import net.minecraft.world.gen.feature.structure.OceanRuinStructure;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class BiomeHelper {

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

    public static BiomeMap createBiomeMap() {
        List<BiomeData> biomes = getAllBiomeData();
        BiomeWeights weights = new BiomeWeights();
        BiomeMap.Builder builder = BiomeMapBuilder.create();
        for (BiomeData data : biomes) {
            Biome biome = (Biome) data.reference;
            if (biome.isMutation() && getId(biome).contains("hills")) {
                continue;
            }

            int weight = weights.getWeight(biome);
            if (BiomePredicate.BEACH.test(data)) {
                builder.addBeach(biome, weight);
            } else if (BiomePredicate.COAST.test(data)) {
                builder.addCoast(biome, weight);
            } else if (biome.getCategory() == Biome.Category.OCEAN) {
                builder.addOcean(biome, weight);
            } else if (BiomePredicate.RIVER.test(data)) {
                builder.addRiver(biome, weight);
            } else if (BiomePredicate.LAKE.test(data)) {
                builder.addLake(biome, weight);
            } else if (BiomePredicate.WETLAND.test(data)) {
                builder.addWetland(biome, weight);
            } else if (BiomePredicate.MOUNTAIN.test(data)) {
                builder.addMountain(biome, weight);
            } else {
                Collection<BiomeType> types = getTypes(data, biome);
                for (BiomeType type : types) {
                    // shouldn't happen
                    if (type == BiomeType.ALPINE) {
                        continue;
                    }
                    builder.addLand(type, biome, weight);
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

    public static Biome.TempCategory getTempCategory(Biome biome) {
        // vanilla ocean biome properties are not at all helpful for determining temperature
        if (biome.getCategory() == Biome.Category.OCEAN) {
            // warm & luke_warm oceans get OceanRuinStructure.Type.WARM
            OceanRuinConfig config = biome.getStructureConfig(Feature.OCEAN_RUIN);
            if (config != null) {
                if (config.field_204031_a == OceanRuinStructure.Type.WARM) {
                    return Biome.TempCategory.WARM;
                }
            }

            // if the id contains the world cold or frozen, assume it's cold
            if (getId(biome).contains("cold") || getId(biome).contains("frozen")) {
                return Biome.TempCategory.COLD;
            }

            // the rest we categorize as medium
            return Biome.TempCategory.MEDIUM;
        }
        // hopefully biomes otherwise have a sensible category
        return biome.getTempCategory();
    }

    public static Biome.TempCategory getMountainCategory(Biome biome) {
        if (biome.getDefaultTemperature() < 0.2) {
            return Biome.TempCategory.COLD;
        }
        if (biome.getDefaultTemperature() > 0.4) {
            return Biome.TempCategory.WARM;
        }
        return Biome.TempCategory.MEDIUM;
    }

    public static String getId(Biome biome) {
        ResourceLocation name = biome.getRegistryName();
        if (name == null) {
            return "unknown";
        }
        return name.toString();
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

    public static List<BiomeData> getAllBiomeData() {
        Collection<Biome> biomes = getAllBiomes();
        Vec2f tempRange = getRange(biomes, Biome::getDefaultTemperature);
        Vec2f moistRange = getRange(biomes, Biome::getDownfall);
        List<BiomeData> list = new LinkedList<>();
        for (Biome biome : biomes) {
            String name = getId(biome);
            float moisture = (biome.getDownfall() - moistRange.x) / (moistRange.y - moistRange.x);
            float temperature = (biome.getDefaultTemperature() - tempRange.x) / (tempRange.y - tempRange.x);
            int color = biome.getSurfaceBuilderConfig().getTop().getMaterial().getColor().colorValue;
            list.add(new BiomeData(name, biome, color, moisture, temperature));
        }
        return list;
    }

    public static Set<Biome> getAllBiomes() {
        Set<Biome> biomes = new HashSet<>();
        for (Biome biome : ForgeRegistries.BIOMES) {
            if (filter(biome)) {
                continue;
            }
            biomes.add(biome.delegate.get());
        }
        return biomes;
    }

    private static boolean filter(Biome biome) {
        if (biome.getCategory() == Biome.Category.NONE) {
            return true;
        }
        if (biome.getCategory() == Biome.Category.THEEND) {
            return true;
        }
        if (biome.getCategory() == Biome.Category.NETHER) {
            return true;
        }
        if (biome == Biomes.MUSHROOM_FIELD_SHORE) {
            return true;
        }
        return !BiomeDictionary.getTypes(biome).contains(BiomeDictionary.Type.OVERWORLD);
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
