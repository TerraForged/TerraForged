package com.terraforged.biome.map.set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.biome.map.defaults.BiomeTemps;
import com.terraforged.biome.map.defaults.DefaultBiome;
import com.terraforged.core.cell.Cell;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class TemperatureSet extends BiomeSet {

    public TemperatureSet(Map<Biome.TempCategory, List<Biome>> map, DefaultBiome defaultBiome) {
        super(BiomeSet.collect(map, 3, e -> e.ordinal() - 1), defaultBiome);
    }

    @Override
    public int getIndex(Cell cell) {
        if (cell.temperature < BiomeTemps.COLD) {
            return 0;
        }
        if (cell.temperature > BiomeTemps.HOT) {
            return 2;
        }
        return 1;
    }

    @Override
    public JsonElement toJson() {
        JsonObject root = new JsonObject();
        for (Biome.TempCategory temp : Biome.TempCategory.values()) {
            int index = temp.ordinal() - 1;
            if (index >= 0 && index < 3) {
                JsonArray array = new JsonArray();
                root.add(temp.name(), array);
                Stream.of(getSet(index)).distinct().map(Biome::getRegistryName).map(Objects::toString).forEach(array::add);
            }
        }
        return root;
    }
}
