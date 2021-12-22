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
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.worldgen.asset.ClimateType;
import it.unimi.dsi.fastutil.objects.Object2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BiomeMapManager {
    private static final BiomeType[] TYPES = BiomeType.values();
    private static final BiomeTypeHolder[] HOLDERS = Stream.of(TYPES).map(BiomeTypeHolder::new).toArray(BiomeTypeHolder[]::new);

    private final Registry<Biome> biomes;
    private final Registry<ClimateType> climateTypes;
    private final List<Biome> overworldBiomes;
    private final Map<BiomeType, WeightMap<Biome>> biomeMap;

    public BiomeMapManager(RegistryAccess access) {
        biomes = access.ownedRegistryOrThrow(Registry.BIOME_REGISTRY);
        climateTypes = access.ownedRegistryOrThrow(ModRegistry.CLIMATE.get());
        overworldBiomes = getOverworldBiomes(biomes, climateTypes);
        biomeMap = buildBiomeMap();
    }

    public Biome get(ResourceKey<Biome> key) {
        return biomes.get(key);
    }

    public Registry<Biome> getBiomes() {
        return biomes;
    }

    public List<Biome> getOverworldBiomes() {
        return overworldBiomes;
    }

    public Map<BiomeType, WeightMap<Biome>> getBiomeMap() {
        return biomeMap;
    }

    private Map<BiomeType, WeightMap<Biome>> buildBiomeMap() {
        var map = getWeightsMap();

        var result = new EnumMap<BiomeType, WeightMap<Biome>>(BiomeType.class);
        for (var entry : map.entrySet()) {
            var values = entry.getValue().keySet().toArray(Biome[]::new);
            var weights = entry.getValue().values().toFloatArray();
            result.put(entry.getKey(), new WeightMap<>(values, weights));
        }

        return result;
    }

    private Map<BiomeType, Object2FloatMap<Biome>> getWeightsMap() {
        var map = new HashMap<BiomeType, Object2FloatMap<Biome>>();
        var registered = new ObjectOpenHashSet<Biome>();

        for (var typeHolder : HOLDERS) {
            var biomeType = climateTypes.get(typeHolder.name);
            if (biomeType == null) {
                map.put(typeHolder.type(), newMutableWeightMap());
            } else {
                var typeMap = getBiomeWeights(biomeType, biomes, registered::add);
                map.put(typeHolder.type(), typeMap);
            }
        }

        for (var biome : overworldBiomes) {
            if (registered.contains(biome)) continue;

            var type = BiomeUtil.getType(biome);
            if (type == null) continue;

            map.computeIfAbsent(type, t -> new Object2FloatLinkedOpenHashMap<>()).put(biome, 1F);
        }

        return map;
    }

    private static Object2FloatMap<Biome> getBiomeWeights(ClimateType type, Registry<Biome> biomes, Consumer<Biome> registered) {
        var map = newMutableWeightMap();

        for (var entry : type.getWeights().object2FloatEntrySet()) {
            var biome = biomes.get(entry.getKey());
            if (biome == null) continue;

            map.put(biome, entry.getFloatValue());
            registered.accept(biome);
        }

        return map;
    }

    private static List<Biome> getOverworldBiomes(Registry<Biome> biomes, Registry<ClimateType> biomeTypes) {
        var list = BiomeUtil.getOverworldBiomes(biomes);
        var added = new ObjectOpenHashSet<>(list);

        for (var type : biomeTypes) {
            for (var key : type.getWeights().keySet()) {
                var biome = biomes.get(key);
                if (biome == null) continue;

                if (added.add(biome)) {
                    list.add(biome);
                }
            }
        }

        list.sort(BiomeUtil.getBiomeSorter(biomes));

        return list;
    }

    private static Object2FloatMap<Biome> newMutableWeightMap() {
        return new Object2FloatLinkedOpenHashMap<>();
    }

    private record BiomeTypeHolder(BiomeType type, ResourceLocation name) {
        public BiomeTypeHolder(BiomeType type) {
            this(type, TerraForged.location(type.name().toLowerCase(Locale.ROOT)));
        }
    }
}
