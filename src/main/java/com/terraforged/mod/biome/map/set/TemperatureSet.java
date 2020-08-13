package com.terraforged.mod.biome.map.set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.core.cell.Cell;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.biome.map.defaults.BiomeTemps;
import com.terraforged.mod.biome.map.defaults.DefaultBiome;
import com.terraforged.mod.biome.utils.TempCategory;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class TemperatureSet extends BiomeSet {

    public TemperatureSet(Map<TempCategory, List<Biome>> map, DefaultBiome.Factory defaultBiome, GameContext context) {
        super(BiomeSet.collect(map, 3, Enum::ordinal, context), defaultBiome.create(context));
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
    public JsonElement toJson(GameContext context) {
        JsonObject root = new JsonObject();
        for (TempCategory temp : TempCategory.values()) {
            JsonArray array = new JsonArray();
            root.add(temp.name(), array);
            Stream.of(getSet(temp.ordinal())).distinct()
                    .map(context.biomes::getName)
                    .map(Objects::toString)
                    .forEach(array::add);
        }
        return root;
    }
}
