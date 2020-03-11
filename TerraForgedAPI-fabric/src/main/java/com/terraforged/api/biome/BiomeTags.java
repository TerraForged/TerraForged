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

package com.terraforged.api.biome;

import com.terraforged.core.world.biome.BiomeType;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class BiomeTags {

    private static int generation;
    private static TagContainer<Biome> collection = new TagContainer<>(
            path -> Optional.empty(),
            "",
            false,
            ""
    );

    private static final Map<BiomeType, BiomeWrapper> tags = new EnumMap<>(BiomeType.class);

    public static final Tag<Biome> ALPINE = tag(BiomeType.ALPINE);
    public static final Tag<Biome> COLD_STEPPE = tag(BiomeType.COLD_STEPPE);
    public static final Tag<Biome> DESERT = tag(BiomeType.DESERT);
    public static final Tag<Biome> GRASSLAND = tag(BiomeType.GRASSLAND);
    public static final Tag<Biome> SAVANNA = tag(BiomeType.SAVANNA);
    public static final Tag<Biome> STEPPE = tag(BiomeType.STEPPE);
    public static final Tag<Biome> TAIGA = tag(BiomeType.TAIGA);
    public static final Tag<Biome> TEMPERATE_FOREST = tag(BiomeType.TEMPERATE_FOREST);
    public static final Tag<Biome> TEMPERATE_RAINFOREST = tag(BiomeType.TEMPERATE_RAINFOREST);
    public static final Tag<Biome> TROPICAL_RAINFOREST = tag(BiomeType.TROPICAL_RAINFOREST);
    public static final Tag<Biome> TUNDRA = tag(BiomeType.TUNDRA);

    public static Tag<Biome> getTag(BiomeType type) {
        return tags.get(type);
    }

    public static void setCollection(TagContainer<Biome> collection) {
        BiomeTags.collection = collection;
        BiomeTags.generation++;
    }

    private static BiomeWrapper tag(BiomeType type) {
        BiomeWrapper wrapper = tag(type.name().toLowerCase());
        tags.put(type, wrapper);
        return wrapper;
    }

    private static BiomeWrapper tag(String name) {
        return new BiomeWrapper(new Identifier("terraforged", name));
    }

    private static class BiomeWrapper extends Tag<Biome> {

        private Tag<Biome> cachedTag = null;
        private int lastKnownGeneration = -1;

        public BiomeWrapper(Identifier name) {
            super(name);
        }

        @Override
        public boolean contains(Biome biome) {
            if (this.lastKnownGeneration != BiomeTags.generation) {
                this.cachedTag = BiomeTags.collection.getOrCreate(this.getId());
                this.lastKnownGeneration = BiomeTags.generation;
            }

            return this.cachedTag.contains(biome);
        }

        @Override
        public Collection<Biome> values() {
            if (this.lastKnownGeneration != BiomeTags.generation) {
                this.cachedTag = BiomeTags.collection.getOrCreate(this.getId());
                this.lastKnownGeneration = BiomeTags.generation;
            }

            return this.cachedTag.values();
        }

        @Override
        public Collection<Tag.Entry<Biome>> entries() {
            if (this.lastKnownGeneration != BiomeTags.generation) {
                this.cachedTag = BiomeTags.collection.getOrCreate(this.getId());
                this.lastKnownGeneration = BiomeTags.generation;
            }

            return this.cachedTag.entries();
        }
    }
}
