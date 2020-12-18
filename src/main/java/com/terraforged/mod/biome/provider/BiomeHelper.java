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

package com.terraforged.mod.biome.provider;

import com.terraforged.mod.featuremanager.GameContext;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.map.BiomeMapBuilder;
import com.terraforged.mod.biome.map.BiomePredicate;
import com.terraforged.mod.biome.map.defaults.BiomeTemps;
import com.terraforged.mod.biome.utils.TempCategory;
import com.terraforged.noise.util.Vec2f;
import com.terraforged.engine.world.climate.biome.BiomeData;
import com.terraforged.engine.world.climate.biome.BiomeType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

import java.util.*;
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

    public static BiomeMap createBiomeMap(GameContext context) {
        List<BiomeData> biomes = getAllBiomeData(context);
        BiomeWeights weights = new BiomeWeights(context);
        BiomeMap.Builder builder = BiomeMapBuilder.create(context);
        for (BiomeData data : biomes) {
            Biome biome = (Biome) data.reference;
            if (context.biomes.getName(biome).contains("hills")) {
                continue;
            }

            int weight = weights.getWeight(biome);
            if (BiomePredicate.BEACH.test(data)) {
                builder.addBeach(biome, weight);
            } else if (BiomePredicate.COAST.test(data)) {
                builder.addCoast(biome, weight);
            } else if (BiomePredicate.OCEAN.test(data)) {
                builder.addOcean(biome, weight);
            } else if (BiomePredicate.RIVER.test(data)) {
                builder.addRiver(biome, weight);
            } else if (BiomePredicate.LAKE.test(data)) {
                builder.addLake(biome, weight);
            } else if (BiomePredicate.WETLAND.test(data)) {
                builder.addWetland(biome, weight);
            } else if (BiomePredicate.VOLCANO.test(data)) {
                builder.addVolcano(biome, weight);
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

        builder.addLand(BiomeType.TEMPERATE_RAINFOREST, context.biomes.get(Biomes.PLAINS), 5);
        builder.addLand(BiomeType.TEMPERATE_FOREST, context.biomes.get(Biomes.FLOWER_FOREST), 2);
        builder.addLand(BiomeType.TEMPERATE_FOREST, context.biomes.get(Biomes.PLAINS), 5);
        builder.addLand(BiomeType.TUNDRA, context.biomes.get(ModBiomes.SNOWY_TAIGA_SCRUB), 2);
        builder.addLand(BiomeType.TAIGA, context.biomes.get(ModBiomes.TAIGA_SCRUB), 2);

        return builder.build();
    }

//        Biomes.MOUNTAINS 0.2F
//        Biomes.GRAVELLY_MOUNTAINS 0.2F
//        Biomes.SNOWY_MOUNTAINS 0.0F
//        Biomes.SNOWY_TAIGA_MOUNTAINS -0.5F
//        Biomes.TAIGA_MOUNTAINS 0.25F
//        Biomes.WOODED_MOUNTAINS 0.2F
//        Biomes.MODIFIED_GRAVELLY_MOUNTAINS 0.2F
    public static TempCategory getMountainCategory(Biome biome) {
        if (getDefaultTemperature(biome) <= BiomeTemps.COLD) {
            return TempCategory.COLD;
        }
        if (getDefaultTemperature(biome) >= BiomeTemps.HOT) {
            return TempCategory.WARM;
        }
        return TempCategory.MEDIUM;
    }

    public static float getDefaultTemperature(Biome biome) {
        return biome.getTemperature();
    }

    public static ISurfaceBuilderConfig getSurface(Biome biome) {
        if (biome != null) {
            if (biome.getGenerationSettings() != null && biome.getGenerationSettings().getSurfaceBuilderConfig() != null) {
                return biome.getGenerationSettings().getSurfaceBuilderConfig();
            }
        }
        return SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG;
    }

    public static ConfiguredSurfaceBuilder<?> getSurfaceBuilder(Biome biome) {
        if (biome != null) {
            if (biome.getGenerationSettings() != null && biome.getGenerationSettings().getSurfaceBuilder() != null) {
                return biome.getGenerationSettings().getSurfaceBuilder().get();
            }
        }
        return SurfaceBuilder.DEFAULT.func_242929_a(SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG);
    }

    public static BiomeGenerationSettings getGenSettings(Biome biome) {
        return biome.getGenerationSettings();
    }

    public static void test(Biome biome) {

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
        Collection<Biome> biomes = getAllBiomes(context);
        Vec2f tempRange = getRange(biomes, BiomeHelper::getDefaultTemperature);
        Vec2f moistRange = getRange(biomes, Biome::getDownfall);
        List<BiomeData> list = new LinkedList<>();
        for (Biome biome : biomes) {
            String name = context.biomes.getName(biome);
            float moisture = (biome.getDownfall() - moistRange.x) / (moistRange.y - moistRange.x);
            float temperature = (getDefaultTemperature(biome) - tempRange.x) / (tempRange.y - tempRange.x);
            int color = getSurface(biome).getTop().getMaterial().getColor().colorValue;
            list.add(new BiomeData(name, biome, color, moisture, temperature));
        }
        return list;
    }

    public static List<Biome> getAllBiomes(GameContext context) {
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
//        return !BiomeDictionary.getTypes(biome).contains(BiomeDictionary.Type.OVERWORLD);
        return false;
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
