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

package com.terraforged.mod.feature.tree;

import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SaplingConfig {

    private static final TypeToken<Feature<DefaultFeatureConfig>> TOKEN = new TypeToken<Feature<DefaultFeatureConfig>>() {
    };

    private final Map<Identifier, Integer> normal;
    private final Map<Identifier, Integer> giant;

    public SaplingConfig() {
        normal = new HashMap<>();
        giant = new HashMap<>();
    }

    public <T> SaplingConfig(T config, DynamicOps<T> ops) {
        this.normal = getSection("normal", config, ops);
        this.giant = getSection("giant", config, ops);
    }

    public SaplingConfig addNormal(Identifier name, int weight) {
        normal.put(name, weight);
        return this;
    }

    public SaplingConfig addNormal(String name, int weight) {
        normal.put(new Identifier(name), weight);
        return this;
    }

    public SaplingConfig addGiant(Identifier name, int weight) {
        giant.put(name, weight);
        return this;
    }

    public SaplingConfig addGiant(String name, int weight) {
        giant.put(new Identifier(name), weight);
        return this;
    }

    private <T> Map<Identifier, Integer> getSection(String key, T config, DynamicOps<T> ops) {
        return ops.get(config, key).flatMap(ops::getMapValues).map(map -> {
            Map<Identifier, Integer> backing = new HashMap<>();

            for (Map.Entry<T, T> entry : map.entrySet()) {
                String name = ops.getStringValue(entry.getKey()).orElse("");
                int weight = ops.getNumberValue(entry.getValue()).orElse(0).intValue();
                if (name.isEmpty() || weight == 0) {
                    continue;
                }

                Identifier loc = new Identifier(name);
                backing.put(loc, weight);
            }

            return backing;
        }).orElse(Collections.emptyMap());
    }

    public List<Feature<DefaultFeatureConfig>> getNormal() {
        return build(normal);
    }

    public List<Feature<DefaultFeatureConfig>> getGiant() {
        return build(giant);
    }

    @SuppressWarnings("unchecked")
    public static List<Feature<DefaultFeatureConfig>> build(Map<Identifier, Integer> map) {
        List<Feature<DefaultFeatureConfig>> list = new LinkedList<>();
        for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {
            Feature<?> feature = Registry.FEATURE.get(entry.getKey());
            if (feature == null) {
                continue;
            }

            if (TOKEN.getRawType().isAssignableFrom(feature.getClass())) {
                Feature<DefaultFeatureConfig> noConfFeature = (Feature<DefaultFeatureConfig>) feature;
                list.add(noConfFeature);
            }
        }
        return list;
    }
}
