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

package com.terraforged.mod.featuremanager.matcher.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.mod.featuremanager.GameContext;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class BiomeMatcherParser {

    public static Optional<BiomeMatcher> parse(JsonObject root, GameContext context) {
        if (root.has("biomes")) {
            return parse(root.get("biomes"), context);
        }
        return Optional.of(BiomeMatcher.ANY);
    }

    public static Optional<BiomeMatcher> parse(JsonElement element, GameContext context) {
        Collector collector = new Collector();
        if (element.isJsonPrimitive()) {
            String biome = element.getAsString();
            if (biome.equals("*")) {
                return Optional.of(BiomeMatcher.ANY);
            }
            collectBiomes(biome, collector, context);
        } else if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                collectBiomes(e.getAsString(), collector, context);
            }
        }
        if (collector.first == null) {
            return Optional.empty();
        }
        if (collector.all.size() == 1) {
            return Optional.of(BiomeMatcher.of(collector.first));
        }
        return Optional.of(BiomeMatcher.of(collector.all));
    }

    public static void collectBiomes(String biome, Collector collector, GameContext context) {
        if (biome.endsWith("*")) {
            biome = biome.substring(0, biome.length() - 1);
            for (Biome b : context.biomes) {
                String name = context.biomes.getName(b);
                if (name.startsWith(biome)) {
                    collector.add(b);
                }
            }
        } else if (biome.contains("*")) {
            String[] parts = biome.split("\\*");
            if (parts.length == 2) {
                for (Biome b : context.biomes) {
                    String name = context.biomes.getName(b);
                    if (name.startsWith(parts[0]) && name.endsWith(parts[1])) {
                        collector.add(b);
                    }
                }
            }
        } else {
            context.biomes.get(new ResourceLocation(biome)).ifPresent(collector);
        }
    }

    public static class Collector implements Consumer<Biome> {

        private Biome first = null;
        private Set<Biome> all = Collections.emptySet();

        public BiomeMatcher create() {
            if (first == null) {
                return BiomeMatcher.ANY;
            }
            if (all.size() == 1) {
                return BiomeMatcher.of(first);
            }
            return BiomeMatcher.of(all);
        }

        @Override
        public void accept(Biome biome) {
            add(biome);
        }

        private void add(Biome biome) {
            if (first == null) {
                first = biome;
            }
            if (all.isEmpty()) {
                all = new HashSet<>();
                all.add(first);
            }
            all.add(biome);
        }
    }
}
