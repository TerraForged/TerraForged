package com.terraforged.app.biome;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.grid.FixedGrid;
import com.terraforged.core.world.biome.BiomeData;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.core.world.terrain.Terrain;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class BiomeProvider {

    private final List<FixedGrid<BiomeData>> biomes;

    public BiomeProvider() {
        biomes = getBiomes(5);
    }

    public BiomeData getBiome(Cell<Terrain> cell) {
        FixedGrid<BiomeData> grid = biomes.get(cell.biomeType.ordinal());
        if (grid == null) {
            return null;
        }
        return grid.get(cell.moisture, cell.temperature, cell.biome);
    }

    private static List<FixedGrid<BiomeData>> getBiomes(int gridSize) {
        List<FixedGrid<BiomeData>> data = new ArrayList<>();
        for (BiomeType type : BiomeType.values()) {
            data.add(type.ordinal(), null);
        }

        Map<String, BiomeData> biomes = loadBiomes();
        Map<BiomeType, List<String>> types = loadBiomeTypes();
        for (Map.Entry<BiomeType, List<String>> e : types.entrySet()) {
            List<BiomeData> list = new LinkedList<>();
            for (String id : e.getValue()) {
                BiomeData biome = biomes.get(id);
                if (biome != null) {
                    list.add(biome);
                }
            }
            FixedGrid<BiomeData> grid = FixedGrid.generate(gridSize, list, b -> b.rainfall, b -> b.temperature);
            data.set(e.getKey().ordinal(), grid);
        }

        return data;
    }

    private static Map<String, BiomeData> loadBiomes() {
        try (InputStream inputStream = BiomeProvider.class.getResourceAsStream("/biome_data.json")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(sb::append);
            JSONArray array = JSONArray.parse(sb.toString());
            Map<String,BiomeData> biomes = new HashMap<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                String name = object.getString("id");
                float moisture = object.getFloat("moisture");
                float temperature = object.getFloat("temperature");
                int color = BiomeColor.getRGB(temperature, moisture);
                BiomeData biome = new BiomeData(name, null, color, moisture, temperature);
                biomes.put(name, biome);
            }
            return biomes;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private static Map<BiomeType, List<String>> loadBiomeTypes() {
        try (InputStream inputStream = BiomeProvider.class.getResourceAsStream("/biome_groups.json")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(sb::append);
            JSONObject object = JSONObject.parse(sb.toString());
            Iterator iterator = object.keyIterator();
            Map<BiomeType, List<String>> biomes = new HashMap<>();
            while (iterator.hasNext()) {
                String key = "" + iterator.next();
                if (key.contains("rivers")) {
                    continue;
                }
                if (key.contains("oceans")) {
                    continue;
                }
                BiomeType type = BiomeType.valueOf(key);
                List<String> group = new LinkedList<>();
                JSONArray array = object.getJSONArray(key);
                for (int i = 0; i < array.size(); i++) {
                    group.add(array.getString(i));
                }
                biomes.put(type, group);
            }
            return biomes;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
}
