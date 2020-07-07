package com.terraforged.mod.biome.map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.core.cell.Cell;
import com.terraforged.mod.biome.map.defaults.DefaultBiomes;
import com.terraforged.mod.biome.map.set.BiomeSet;
import com.terraforged.mod.biome.map.set.BiomeTypeSet;
import com.terraforged.mod.biome.map.set.RiverSet;
import com.terraforged.mod.biome.map.set.TemperatureSet;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.TerrainType;
import net.minecraft.world.biome.Biome;

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
    private final BiomeSet land;
    private final BiomeSet mountains;
    private final BiomeSet[] terrainBiomes;

    public SimpleBiomeMap(BiomeMapBuilder builder) {
        deepOcean = new TemperatureSet(builder.deepOceans, DefaultBiomes::defaultDeepOcean);
        shallowOcean = new TemperatureSet(builder.oceans, DefaultBiomes::defaultOcean);
        beach = new TemperatureSet(builder.beaches, DefaultBiomes::defaultBeach);
        coast = new TemperatureSet(builder.coasts, DefaultBiomes::defaultBiome);
        river = new RiverSet(builder.rivers, DefaultBiomes::defaultRiver, this);
        lake = new TemperatureSet(builder.lakes, DefaultBiomes::defaultLake);
        wetland = new TemperatureSet(builder.wetlands, DefaultBiomes::defaultWetland);
        mountains = new TemperatureSet(builder.mountains, DefaultBiomes::defaultMountain);
        land = new BiomeTypeSet(builder.map, DefaultBiomes::defaultBiome);
        terrainBiomes = new BiomeSet[TerrainType.values().length];
        terrainBiomes[TerrainType.SHALLOW_OCEAN.ordinal()] = shallowOcean;
        terrainBiomes[TerrainType.DEEP_OCEAN.ordinal()] = deepOcean;
        terrainBiomes[TerrainType.WETLAND.ordinal()] = wetland;
        terrainBiomes[TerrainType.RIVER.ordinal()] = river;
        terrainBiomes[TerrainType.LAKE.ordinal()] = lake;
        for (TerrainType type : TerrainType.values()) {
            if (terrainBiomes[type.ordinal()] == null) {
                terrainBiomes[type.ordinal()] = land;
            }
        }
    }

    @Override
    public Biome provideBiome(Cell cell, Levels levels) {
        TerrainType type = cell.terrain.getType();
        if (type.isSubmerged() && cell.value > levels.water) {
            return land.getBiome(cell);
        }
        return terrainBiomes[cell.terrain.getType().ordinal()].getBiome(cell);
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
    public Biome getCoast(Cell cell) {
        // treat land & coast biome-sets as one combined set
        Biome[] inland = land.getSet(cell);
        Biome[] coastal = coast.getSet(cell);

        // calculate where in the combined set the cell.biome points
        int maxIndex = inland.length + coastal.length - 1;
        int index = NoiseUtil.round(maxIndex * cell.biomeIdentity);

        // if index lies within the coast section of the set
        if (index >= inland.length) {
            // relativize the index to start at 0
            index -= inland.length;

            // shouldn't be required but check that index is within bounds
            if (index < coastal.length) {
                return coastal[index];
            }
        }

        return DefaultBiomes.NONE;
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

    @Override
    public Biome getLand(Cell cell) {
        return land.getBiome(cell);
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
    public JsonElement toJson() {
        JsonObject root = new JsonObject();
        root.add("DEEP_OCEAN", deepOcean.toJson());
        root.add("SHALLOW_OCEAN", shallowOcean.toJson());
        root.add("BEACH", beach.toJson());
        root.add("COAST", coast.toJson());
        root.add("RIVER", river.toJson());
        root.add("LAKE", lake.toJson());
        root.add("WETLAND", wetland.toJson());
        root.add("LAND", landToJson());
        return root;
    }

    private JsonElement landToJson() {
        JsonObject root = land.toJson().getAsJsonObject();
        root.add(BiomeType.ALPINE.name(), mountains.toJson());
        return root;
    }
}
