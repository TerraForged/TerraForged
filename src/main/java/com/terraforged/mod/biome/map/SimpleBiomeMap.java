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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.engine.cell.Cell;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.biome.map.defaults.DefaultBiomes;
import com.terraforged.mod.biome.map.set.*;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.engine.world.climate.biome.BiomeType;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.terrain.TerrainCategory;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleBiomeMap implements BiomeMap {

    private final BiomeSet deepOcean;
    private final BiomeSet shallowOcean;
    private final BiomeSet beach;
    private final BiomeSet coast;
    private final BiomeSet river;
    private final BiomeSet lake;
    private final BiomeSet wetland;
    private final BiomeTypeSet land;
    private final BiomeSet mountains;
    private final BiomeSet volcanoes;
    private final BiomeSet[] terrainBiomes;

    public SimpleBiomeMap(BiomeMapBuilder builder) {
        deepOcean = new TemperatureSet(builder.deepOceans, DefaultBiomes.DEEP_OCEAN, builder.context);
        shallowOcean = new TemperatureSet(builder.oceans, DefaultBiomes.OCEAN, builder.context);
        beach = new TemperatureSet(builder.beaches, DefaultBiomes.BEACH, builder.context);
        coast = new TemperatureSet(builder.coasts, DefaultBiomes.LAND, builder.context);
        river = new RiverSet(builder.rivers, DefaultBiomes.RIVER, this, builder.context);
        lake = new TemperatureSet(builder.lakes, DefaultBiomes.LAKE, builder.context);
        wetland = new WetlandSet(builder.wetlands, this, builder.context);
        mountains = new TemperatureSet(builder.mountains, DefaultBiomes.MOUNTAIN, builder.context);
        volcanoes = new TemperatureSet(builder.volcanoes, DefaultBiomes.VOLCANOES, builder.context);
        land = new BiomeTypeSet(builder.map, DefaultBiomes.LAND, builder.context);
        terrainBiomes = new BiomeSet[TerrainCategory.values().length];
        terrainBiomes[TerrainCategory.SHALLOW_OCEAN.ordinal()] = shallowOcean;
        terrainBiomes[TerrainCategory.DEEP_OCEAN.ordinal()] = deepOcean;
        terrainBiomes[TerrainCategory.WETLAND.ordinal()] = wetland;
        terrainBiomes[TerrainCategory.RIVER.ordinal()] = river;
        terrainBiomes[TerrainCategory.LAKE.ordinal()] = lake;
        for (TerrainCategory type : TerrainCategory.values()) {
            if (terrainBiomes[type.ordinal()] == null) {
                terrainBiomes[type.ordinal()] = land;
            }
        }
    }

    @Override
    public Biome provideBiome(Cell cell, Levels levels) {
        TerrainCategory type = cell.terrain.getCategory();
        if (type.isSubmerged() && cell.value > levels.water) {
            return land.getBiome(cell);
        }
        return terrainBiomes[type.ordinal()].getBiome(cell);
    }

    @Override
    public Biome getDeepOcean(Cell cell) {
        return deepOcean.getBiome(cell);
    }

    @Override
    public Biome getShallowOcean(Cell cell) {
        return shallowOcean.getBiome(cell);
    }

    @Override
    public Biome getBeach(Cell cell) {
        return beach.getBiome(cell);
    }

    @Override
    @Nullable
    public Biome getCoast(Cell cell) {
        // treat land & coast biome-sets as one combined set
        Biome[] inland = land.getSet(cell);
        Biome[] coastal = coast.getSet(cell);

        // calculate where in the combined set the cell.biome points
        int maxIndex = inland.length + coastal.length - 1;
        int index = NoiseUtil.round(maxIndex * cell.biomeRegionId);

        // if index lies within the coast section of the set
        if (index >= inland.length) {
            // relativize the index to start at 0
            index -= inland.length;

            // shouldn't be required but check that index is within bounds
            if (index < coastal.length) {
                return coastal[index];
            }
        }

        return null;
    }

    @Override
    public Biome getRiver(Cell cell) {
        return river.getBiome(cell);
    }

    @Override
    public Biome getLake(Cell cell) {
        return lake.getBiome(cell);
    }

    @Override
    public Biome getWetland(Cell cell) {
        return wetland.getBiome(cell);
    }

    @Override
    public Biome getMountain(Cell cell) {
        return mountains.getBiome(cell);
    }

    @Nullable
    @Override
    public Biome getVolcano(Cell cell) {
        return volcanoes.getBiome(cell);
    }

    @Override
    public Biome getLand(Cell cell) {
        return land.getBiome(cell);
    }

    @Override
    public BiomeTypeSet getLandSet() {
        return land;
    }

    @Override
    public List<Biome> getAllBiomes(BiomeType type) {
        if (type == BiomeType.ALPINE) {
            return Collections.emptyList();
        }
        int size = land.getSize(type.ordinal());
        if (size == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(land.getSet(type.ordinal()));
    }

    @Override
    public JsonElement toJson(GameContext context) {
        JsonObject root = new JsonObject();
        root.add("DEEP_OCEAN", deepOcean.toJson(context));
        root.add("SHALLOW_OCEAN", shallowOcean.toJson(context));
        root.add("BEACH", beach.toJson(context));
        root.add("COAST", coast.toJson(context));
        root.add("RIVER", river.toJson(context));
        root.add("LAKE", lake.toJson(context));
        root.add("WETLAND", wetland.toJson(context));
        root.add("LAND", landToJson(context));
        return root;
    }

    private JsonElement landToJson(GameContext context) {
        JsonObject root = land.toJson(context).getAsJsonObject();
        root.add(BiomeType.ALPINE.name(), mountains.toJson(context));
        return root;
    }
}
