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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.core.cell.Cell;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.biome.map.defaults.DefaultBiome;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.world.biome.BiomeType;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class BiomeTypeSet extends BiomeSet {

    public BiomeTypeSet(Map<BiomeType, List<Biome>> map, DefaultBiome.Factory defaultBiome, GameContext context) {
        super(BiomeSet.collect(map, BiomeType.values().length, Enum::ordinal, context), defaultBiome.create(context));
    }

    public Biome getBiome(BiomeType type, float temperature, float identity) {
        Biome[] set = getSet(type.ordinal());
        if (set.length == 0) {
            return defaultBiome.getDefaultBiome(temperature);
        }

        int maxIndex = set.length - 1;
        int index = NoiseUtil.round(maxIndex * identity);

        // shouldn't happen but safety check the bounds
        if (index < 0 || index >= set.length) {
            return defaultBiome.getDefaultBiome(temperature);
        }

        return set[index];
    }

    @Override
    public int getIndex(Cell cell) {
        return cell.biomeType.ordinal();
    }

    @Override
    public JsonElement toJson(GameContext context) {
        JsonObject root = new JsonObject();
        for (BiomeType type : BiomeType.values()) {
            JsonArray array = new JsonArray();
            root.add(type.name(), array);
            Stream.of(getSet(type.ordinal())).distinct()
                    .map(context.biomes::getName)
                    .map(Objects::toString).forEach(array::add);
        }
        return root;
    }
}
