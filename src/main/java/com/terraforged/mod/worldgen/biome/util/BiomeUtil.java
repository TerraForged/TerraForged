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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.util.*;
import java.util.stream.Collectors;

public class BiomeUtil {
    private static final Map<BiomeType, ResourceLocation> TYPE_NAMES = new EnumMap<>(BiomeType.class);

    private static final Comparator<ResourceKey<?>> KEY_COMPARATOR = Comparator.comparing(ResourceKey::location);

    public static Comparator<Holder<Biome>> BIOME_SORTER = (o1, o2) -> {
        var k1 = o1.unwrapKey().orElseThrow();
        var k2 = o2.unwrapKey().orElseThrow();
        Objects.requireNonNull(k1);
        Objects.requireNonNull(k2);
        return KEY_COMPARATOR.compare(k1, k2);
    };

    static {
        for (var type : BiomeType.values()) {
            TYPE_NAMES.put(type, TerraForged.location(type.name().toLowerCase(Locale.ROOT)));
        }
    }

    public static ResourceLocation getRegistryName(BiomeType type) {
        return TYPE_NAMES.get(type);
    }

    public static List<Holder<Biome>> getOverworldBiomes(RegistryAccess access) {
        return getOverworldBiomes(access.registryOrThrow(Registry.BIOME_REGISTRY));
    }

    public static List<Holder<Biome>> getOverworldBiomes(Registry<Biome> biomes) {
        var holders = new ObjectArrayList<Holder<Biome>>();

        for (var biome : biomes.asHolderIdMap()) {
            if (biome.is(BiomeTags.IS_OVERWORLD)) {
                holders.add(biome);
            }
        }

        holders.sort(BIOME_SORTER);

        return holders;
    }

    public static BiomeType getType(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.HAS_VILLAGE_DESERT))
            return BiomeType.DESERT;
        if (biome.is(BiomeTags.IS_SAVANNA))
            return BiomeType.SAVANNA;
        if (biome.is(BiomeTags.IS_JUNGLE))
            return BiomeType.TROPICAL_RAINFOREST;
        if (biome.is(BiomeTags.IS_TAIGA))
            return getByTemp(biome.value(), BiomeType.TUNDRA, BiomeType.TAIGA);
        if (biome.is(BiomeTags.IS_FOREST))
            return getByRain(biome.value(), BiomeType.TUNDRA, BiomeType.TEMPERATE_RAINFOREST, BiomeType.TEMPERATE_FOREST);
        return BiomeType.GRASSLAND;
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

    private static Set<Holder<Biome>> getVanillaOverworldBiomes(Registry<Biome> biomes) {
        return MultiNoiseBiomeSource.Preset.OVERWORLD.possibleBiomes()
                .map(biomes::getOrCreateHolderOrThrow)
                .collect(Collectors.toSet());
    }
}
