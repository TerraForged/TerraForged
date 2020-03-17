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

package com.terraforged.mod.biome.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.terraforged.core.util.grid.FixedList;
import com.terraforged.core.util.grid.MappedList;
import com.terraforged.core.world.biome.BiomeType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GridBiomeMap extends AbstractBiomeMap {

    private final BiomeGroup[] biomeTypes;

    GridBiomeMap(BiomeMapBuilder builder) {
        super(builder);
        biomeTypes = builder.biomeGroups();
    }

    @Override
    public Biome getBiome(BiomeType type, float temperature, float moisture, float shape) {
        BiomeGroup group = biomeTypes[type.ordinal()];
        if (group == null) {
            return Biomes.NETHER_WASTES;
        }
        return group.biomes.get(moisture, temperature, shape);
    }

    @Override
    public List<Biome> getAllBiomes(BiomeType type) {
        BiomeGroup group = biomeTypes[type.ordinal()];
        if (group == null) {
            return Collections.emptyList();
        }
        List<Biome> biomes = new LinkedList<>();
        for (MappedList<FixedList<Biome>> row : group.biomes) {
            for (FixedList<Biome> cell : row) {
                for (Biome biome : cell) {
                    biomes.add(biome);
                }
            }
        }
        return biomes;
    }

    @Override
    public Set<Biome> getBiomes(BiomeType type) {
        BiomeGroup group = biomeTypes[type.ordinal()];
        if (group == null) {
            return Collections.emptySet();
        }
        Set<Biome> biomes = new HashSet<>();
        for (MappedList<FixedList<Biome>> row : group.biomes) {
            for (FixedList<Biome> cell : row) {
                for (Biome biome : cell) {
                    biomes.add(biome);
                }
            }
        }
        return biomes;
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (BiomeType type : BiomeType.values()) {
            BiomeGroup group = biomeTypes[type.ordinal()];
            JsonObject grid = new JsonObject();
            if (group != null) {
                int rowCount = 0;
                float maxRow = group.biomes.size() - 1;
                for (MappedList<FixedList<Biome>> row : group.biomes) {
                    int colCount = 0;
                    float maxCol = row.size() - 1;

                    JsonObject rowJson = new JsonObject();
                    for (FixedList<Biome> cell : row) {
                        JsonArray colJson = new JsonArray();
                        for (Biome biome : cell.uniqueValues()) {
                            colJson.add(String.valueOf(Registry.BIOME.getId(biome)));
                        }
                        float colId = colCount / maxCol;
                        rowJson.add("" + colId, colJson);
                        colCount++;
                    }

                    float rowId = rowCount / maxRow;
                    grid.add("" + rowId, rowJson);
                    rowCount++;
                }
            }
            root.add(type.name(), grid);
        }
        return root;
    }
}
