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

package com.terraforged.biome.map;

import com.terraforged.biome.provider.BiomeHelper;
import com.terraforged.core.util.grid.FixedGrid;
import com.terraforged.world.biome.BiomeData;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.util.ListUtils;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BiomeMapBuilder implements BiomeMap.Builder {

    private final Map<Biome.TempCategory, List<Biome>> rivers = new HashMap<>();
    private final Map<Biome.TempCategory, List<Biome>> lakes = new HashMap<>();
    private final Map<Biome.TempCategory, List<Biome>> wetlands = new HashMap<>();
    private final Map<Biome.TempCategory, List<Biome>> beaches = new HashMap<>();
    private final Map<Biome.TempCategory, List<Biome>> oceans = new HashMap<>();
    private final Map<Biome.TempCategory, List<Biome>> deepOceans = new HashMap<>();
    private final Map<BiomeType, List<Biome>> map = new EnumMap<>(BiomeType.class);
    private final Map<Biome, BiomeData> dataMap = new HashMap<>();

    private final int gridSize;
    private final Function<BiomeMapBuilder, BiomeMap> constructor;

    BiomeMapBuilder(Function<BiomeMapBuilder, BiomeMap> constructor, int gridSize, List<BiomeData> biomes) {
        this.constructor = constructor;
        this.gridSize = gridSize;

        for (BiomeData data : biomes) {
            dataMap.put((Biome) data.reference, data);
        }
    }

    @Override
    public BiomeMapBuilder addOcean(Biome biome, int count) {
        Biome.TempCategory category = BiomeHelper.getTempCategory(biome);
        if (biome.getDepth() < -1) {
            add(deepOceans.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        } else {
            add(oceans.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        }
        return this;
    }

    @Override
    public BiomeMap.Builder addBeach(Biome biome, int count) {
        Biome.TempCategory category = BiomeHelper.getTempCategory(biome);
        add(beaches.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addRiver(Biome biome, int count) {
        Biome.TempCategory category = BiomeHelper.getTempCategory(biome);
        add(rivers.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addLake(Biome biome, int count) {
        Biome.TempCategory category = BiomeHelper.getTempCategory(biome);
        add(lakes.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addWetland(Biome biome, int count) {
        Biome.TempCategory category = BiomeHelper.getTempCategory(biome);
        add(wetlands.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addBiome(BiomeType type, Biome biome, int count) {
        add(map.computeIfAbsent(type, t -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMap build() {
        return constructor.apply(this);
    }

    Biome[][] rivers() {
        return collectTemps(rivers);
    }

    Biome[][] lakes() {
        return collectTemps(lakes);
    }

    Biome[][] wetlands() {
        return collectTemps(wetlands);
    }

    Biome[][] beaches() {
        return collectTemps(beaches);
    }

    Biome[][] oceans() {
        return collectTemps(oceans);
    }

    Biome[][] deepOceans() {
        return collectTemps(deepOceans);
    }

    Biome[][] biomeList() {
        return collectTypes(map);
    }

    BiomeGroup[] biomeGroups() {
        BiomeGroup[] biomes = new BiomeGroup[BiomeType.values().length];

        Function<Biome, Float> moisture = b -> dataMap.get(b).rainfall;
        Function<Biome, Float> temperature = b -> dataMap.get(b).temperature;
        for (BiomeType type : BiomeType.values()) {
            List<Biome> list = map.getOrDefault(type, Collections.emptyList());
            if (list.isEmpty()) {
                continue;
            }
            FixedGrid<Biome> grid = FixedGrid.generate(gridSize, list, moisture, temperature);
            biomes[type.ordinal()] = new BiomeGroup(grid);
        }

        return biomes;
    }

    private void add(List<Biome> list, Biome biome, int count) {
        for (int i = 0; i < count; i++) {
            list.add(biome);
        }
    }

    private Biome[][] collectTemps(Map<Biome.TempCategory, List<Biome>> map) {
        Biome[][] biomes = new Biome[3][];
        for (Biome.TempCategory category : Biome.TempCategory.values()) {
            if (category == Biome.TempCategory.OCEAN) {
                continue;
            }
            List<Biome> list = map.getOrDefault(category, Collections.emptyList());
            list = ListUtils.minimize(list);
            list.sort(Comparator.comparing(BiomeHelper::getId));
            biomes[category.ordinal() - 1] = list.toArray(new Biome[0]);
        }
        return biomes;
    }

    private Biome[][] collectTypes(Map<BiomeType, List<Biome>> map) {
        Biome[][] biomes = new Biome[BiomeType.values().length][];
        for (BiomeType type : BiomeType.values()) {
            List<Biome> list = map.getOrDefault(type, Collections.emptyList());
            list = ListUtils.minimize(list);
            list.sort(Comparator.comparing(BiomeHelper::getId));
            biomes[type.ordinal()] = list.toArray(new Biome[0]);
        }
        return biomes;
    }

    public static BiomeMap.Builder basic(List<BiomeData> biomes) {
        return new BiomeMapBuilder(BasicBiomeMap::new, 0, biomes);
    }

    public static BiomeMap.Builder grid(int size, List<BiomeData> biomes) {
        return new BiomeMapBuilder(GridBiomeMap::new, size, biomes);
    }
}
