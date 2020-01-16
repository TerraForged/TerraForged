package com.terraforged.core.settings;

import com.terraforged.core.util.serialization.annotation.Comment;
import com.terraforged.core.util.serialization.annotation.Range;
import com.terraforged.core.util.serialization.annotation.Serializable;
import com.terraforged.core.world.biome.BiomeType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BiomeSettings {

    public BiomeGroup desert = new BiomeGroup(BiomeType.DESERT);
    public BiomeGroup steppe = new BiomeGroup(BiomeType.STEPPE);
    public BiomeGroup coldSteppe = new BiomeGroup(BiomeType.COLD_STEPPE);
    public BiomeGroup grassland = new BiomeGroup(BiomeType.GRASSLAND);
    public BiomeGroup savanna = new BiomeGroup(BiomeType.SAVANNA);
    public BiomeGroup taiga = new BiomeGroup(BiomeType.TAIGA);
    public BiomeGroup temperateForest = new BiomeGroup(BiomeType.TEMPERATE_FOREST);
    public BiomeGroup temperateRainForest = new BiomeGroup(BiomeType.TEMPERATE_RAINFOREST);
    public BiomeGroup tropicalRainForest = new BiomeGroup(BiomeType.TROPICAL_RAINFOREST);
    public BiomeGroup tundra = new BiomeGroup(BiomeType.TUNDRA);

    public List<BiomeGroup> asList() {
        return Arrays.asList(
                desert,
                steppe,
                coldSteppe,
                grassland,
                savanna,
                taiga,
                temperateForest,
                temperateRainForest,
                tropicalRainForest,
                tundra
        );
    }

    public Map<BiomeType, BiomeGroup> asMap() {
        return asList().stream().collect(Collectors.toMap(g -> g.type, g -> g));
    }

    @Serializable
    public static class BiomeGroup {

        public BiomeType type;

        public BiomeWeight[] biomes = new BiomeWeight[0];

        public BiomeGroup() {

        }

        public BiomeGroup(BiomeType biomeType) {
            this.type = biomeType;
        }
    }

    @Serializable
    public static class BiomeWeight {

        public String id;

        @Range(min = 0, max = 50)
        @Comment("Controls how common this biome type is")
        public int weight;

        public BiomeWeight() {

        }

        public BiomeWeight(String id, int weight) {
            this.id = id;
            this.weight = weight;
        }
    }
}
