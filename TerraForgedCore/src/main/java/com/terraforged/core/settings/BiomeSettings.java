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
