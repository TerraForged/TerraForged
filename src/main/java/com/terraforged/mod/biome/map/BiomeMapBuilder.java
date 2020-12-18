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

package com.terraforged.mod.biome.map;

import com.terraforged.mod.featuremanager.GameContext;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.provider.BiomeHelper;
import com.terraforged.mod.biome.utils.TempCategory;
import com.terraforged.engine.world.climate.biome.BiomeType;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.*;
import java.util.function.Function;

public class BiomeMapBuilder implements BiomeMap.Builder {

    protected final Map<TempCategory, List<Biome>> rivers = new HashMap<>();
    protected final Map<TempCategory, List<Biome>> lakes = new HashMap<>();
    protected final Map<TempCategory, List<Biome>> coasts = new HashMap<>();
    protected final Map<TempCategory, List<Biome>> beaches = new HashMap<>();
    protected final Map<TempCategory, List<Biome>> oceans = new HashMap<>();
    protected final Map<TempCategory, List<Biome>> deepOceans = new HashMap<>();
    protected final Map<TempCategory, List<Biome>> mountains = new HashMap<>();
    protected final Map<TempCategory, List<Biome>> volcanoes = new HashMap<>();
    protected final Map<TempCategory, List<Biome>> wetlands = new HashMap<>();
    protected final Map<BiomeType, List<Biome>> map = new EnumMap<>(BiomeType.class);

    protected final GameContext context;
    private final Function<BiomeMapBuilder, BiomeMap> constructor;

    BiomeMapBuilder(GameContext context, Function<BiomeMapBuilder, BiomeMap> constructor) {
        this.context = context;
        this.constructor = constructor;
    }

    @Override
    public BiomeMapBuilder addOcean(Biome biome, int count) {
        TempCategory category = TempCategory.forBiome(biome, context);
        if (biome.getDepth() < -1) {
            add(deepOceans.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        } else {
            add(oceans.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        }
        return this;
    }

    @Override
    public BiomeMap.Builder addBeach(Biome biome, int count) {
        TempCategory category = TempCategory.forBiome(biome, context);
        add(beaches.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMap.Builder addCoast(Biome biome, int count) {
        TempCategory category = TempCategory.forBiome(biome, context);
        add(coasts.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addRiver(Biome biome, int count) {
        TempCategory category = TempCategory.forBiome(biome, context);
        add(rivers.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addLake(Biome biome, int count) {
        TempCategory category = TempCategory.forBiome(biome, context);
        add(lakes.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addWetland(Biome biome, int count) {
        TempCategory category = TempCategory.forBiome(biome, context);
        add(wetlands.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addMountain(Biome biome, int count) {
        TempCategory category = BiomeHelper.getMountainCategory(biome);
        add(mountains.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addVolcano(Biome biome, int count) {
        TempCategory category = TempCategory.forBiome(biome, context);
        add(volcanoes.computeIfAbsent(category, c -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMapBuilder addLand(BiomeType type, Biome biome, int count) {
        add(map.computeIfAbsent(type, t -> new ArrayList<>()), biome, count);
        return this;
    }

    @Override
    public BiomeMap build() {
        makeSafe();
        return constructor.apply(this);
    }

    private void makeSafe() {
        addIfEmpty(rivers, Biomes.RIVER, TempCategory.class);
        addIfEmpty(lakes, ModBiomes.LAKE, TempCategory.class);
        addIfEmpty(coasts, Biomes.STONE_SHORE, TempCategory.class);
        addIfEmpty(beaches, Biomes.BEACH, TempCategory.class);
        addIfEmpty(oceans, Biomes.OCEAN, TempCategory.class);
        addIfEmpty(deepOceans, Biomes.DEEP_OCEAN, TempCategory.class);
        addIfEmpty(wetlands, Biomes.SWAMP, TempCategory.class);
        addIfEmpty(map, Biomes.PLAINS, BiomeType.class);
    }

    private void add(List<Biome> list, Biome biome, int count) {
        if (biome != null) {
            for (int i = 0; i < count; i++) {
                list.add(biome);
            }
        }
    }

    private <E extends Enum<E>> void addIfEmpty(Map<E, List<Biome>> map, RegistryKey<Biome> biome, Class<E> enumType) {
        for (E e : enumType.getEnumConstants()) {
            addIfEmpty(map.computeIfAbsent(e, t -> new ArrayList<>()), context.biomes.get(biome));
        }
    }

    private void addIfEmpty(List<Biome> list, Biome biome) {
        if (list.isEmpty()) {
            list.add(biome);
        }
    }

    public static BiomeMap.Builder create(GameContext context) {
        return new BiomeMapBuilder(context, SimpleBiomeMap::new);
    }
}
