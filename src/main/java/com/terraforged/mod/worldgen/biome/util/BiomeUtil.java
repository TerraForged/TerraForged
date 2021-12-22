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

package com.terraforged.mod.worldgen.biome.util;

import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.util.*;

public class BiomeUtil {
    private static final Map<BiomeType, ResourceLocation> TYPE_NAMES = new EnumMap<>(BiomeType.class);
    private static final Set<String> KNOWN_NAMESPACES = Set.of(TerraForged.MODID, "terralith"); // TODO: Not hardcoded

    static {
        for (var type : BiomeType.values()) {
            TYPE_NAMES.put(type, TerraForged.location(type.name().toLowerCase(Locale.ROOT)));
        }
    }

    public static ResourceLocation getRegistryName(BiomeType type) {
        return TYPE_NAMES.get(type);
    }

    public static List<Biome> getOverworldBiomes(RegistryAccess access) {
        return getOverworldBiomes(access.registryOrThrow(Registry.BIOME_REGISTRY));
    }

    public static List<Biome> getOverworldBiomes(Registry<Biome> biomes) {
        var overworld = getVanillaOverworldBiomes(biomes);
        var platform = Platform.ACTIVE_PLATFORM.get();

        for (var entry : biomes.entrySet()) {
            var biome = entry.getValue();
            var name = entry.getKey().location();

            if (platform.getData().isOverworldBiome(entry.getKey())) {
                overworld.add(biome);
            } else if (KNOWN_NAMESPACES.contains(name.getNamespace())) {
                overworld.add(biome);
            }
        }

        overworld.sort(getBiomeSorter(biomes));

        return overworld;
    }

    public static Comparator<Biome> getBiomeSorter(Registry<Biome> biomes) {
        return (o1, o2) -> {
            var k1 = biomes.getKey(o1);
            var k2 = biomes.getKey(o2);
            Objects.requireNonNull(k1);
            Objects.requireNonNull(k2);
            return k1.compareTo(k2);
        };
    }

    public static BiomeType getType(Biome biome) {
        return switch (biome.getBiomeCategory()) {
            case MESA, DESERT -> BiomeType.DESERT;
            case PLAINS -> getByTemp(biome, BiomeType.COLD_STEPPE, BiomeType.GRASSLAND, BiomeType.STEPPE);
            case TAIGA -> getByTemp(biome, BiomeType.TUNDRA, BiomeType.TAIGA);
            case ICY -> BiomeType.TUNDRA;
            case SAVANNA -> BiomeType.SAVANNA;
            case JUNGLE -> BiomeType.TROPICAL_RAINFOREST;
            case FOREST -> getByRain(biome, BiomeType.TUNDRA, BiomeType.TEMPERATE_RAINFOREST, BiomeType.TEMPERATE_FOREST);
            case MOUNTAIN -> BiomeType.ALPINE;
            default -> null;
        };
    }

    public static BiomeType getByRain(Biome biome, BiomeType frozen, BiomeType wetter, BiomeType dryer) {
        if (biome.getPrecipitation() == Biome.Precipitation.SNOW) return frozen;

        return biome.getDownfall() >= 0.8 ? wetter : dryer;
    }

    public static BiomeType getByTemp(Biome biome, BiomeType colder, BiomeType warmer) {
        return biome.getPrecipitation() == Biome.Precipitation.SNOW ? colder : warmer;
    }

    public static BiomeType getByTemp(Biome biome, BiomeType cold, BiomeType temperate, BiomeType hot) {
        if (biome.getPrecipitation() == Biome.Precipitation.SNOW) return cold;
        if (biome.getPrecipitation() == Biome.Precipitation.NONE || biome.getBaseTemperature() > 1.0) return hot;
        return temperate;
    }

    private static List<Biome> getVanillaOverworldBiomes(Registry<Biome> biomes) {
        var set = new HashSet<>(MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomes).possibleBiomes());
        set.add(biomes.get(Biomes.MEADOW));
        return new ArrayList<>(set);
    }
}
