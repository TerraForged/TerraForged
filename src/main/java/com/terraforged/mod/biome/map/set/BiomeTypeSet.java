package com.terraforged.mod.biome.map.set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.core.cell.Cell;
import com.terraforged.mod.biome.map.defaults.DefaultBiome;
import com.terraforged.world.biome.BiomeType;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class BiomeTypeSet extends BiomeSet {

    public BiomeTypeSet(Map<BiomeType, List<Biome>> map, DefaultBiome defaultBiome) {
        super(BiomeSet.collect(map, BiomeType.values().length, Enum::ordinal), defaultBiome);
    }

    @Override
    public int getIndex(Cell cell) {
        return cell.biomeType.ordinal();
    }

    @Override
    public JsonElement toJson() {
        JsonObject root = new JsonObject();
        for (BiomeType type : BiomeType.values()) {
            JsonArray array = new JsonArray();
            root.add(type.name(), array);
            Stream.of(getSet(type.ordinal())).distinct().map(Biome::getRegistryName).map(Objects::toString).forEach(array::add);
        }
        return root;
    }
}
