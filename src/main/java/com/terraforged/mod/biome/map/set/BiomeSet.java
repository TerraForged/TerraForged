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

package com.terraforged.mod.biome.map.set;

import com.google.gson.JsonElement;
import com.terraforged.core.cell.Cell;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.biome.map.defaults.DefaultBiome;
import com.terraforged.mod.util.ListUtils;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BiomeSet {

    private static final Biome[] EMPTY = new Biome[0];

    protected final Biome[][] biomes;
    protected final DefaultBiome defaultBiome;

    public BiomeSet(Biome[][] biomes, DefaultBiome defaultBiome) {
        this.biomes = biomes;
        this.defaultBiome = defaultBiome;
    }

    public int getSize(int index) {
        return biomes[index].length;
    }

    public int getSize(Cell cell) {
        return biomes[getIndex(cell)].length;
    }

    public Biome[] getSet(int index) {
        return biomes[index];
    }

    public Biome[] getSet(Cell cell) {
        return biomes[getIndex(cell)];
    }

    public Biome getBiome(Cell cell) {
        Biome[] set = biomes[getIndex(cell)];
        if (set.length == 0) {
            return defaultBiome.getDefaultBiome(cell);
        }

        int maxIndex = set.length - 1;
        int index = NoiseUtil.round(maxIndex * cell.biomeRegionId);

        // shouldn't happen but safety check the bounds
        if (index < 0 || index >= set.length) {
            return defaultBiome.getDefaultBiome(cell);
        }

        return set[index];
    }

    public abstract int getIndex(Cell cell);

    public abstract JsonElement toJson(GameContext context);

    protected static Biome[][] collect(Map<? extends Enum<?>, List<Biome>> map, int size, Function<Enum<?>, Integer> indexer, GameContext context) {
        Biome[][] biomes = new Biome[size][];
        for (Enum<?> type : map.keySet()) {
            int index = indexer.apply(type);
            if (index < 0 || index >= size) {
                continue;
            }
            List<Biome> list = map.getOrDefault(type, Collections.emptyList());
            list = ListUtils.minimize(list);
            list.sort(Comparator.comparing(context.biomes::getName));
            biomes[index] = list.toArray(new Biome[0]);
        }
        for (int i = 0; i < size; i++) {
            if (biomes[i] == null) {
                biomes[i] = EMPTY;
            }
        }
        return biomes;
    }
}
