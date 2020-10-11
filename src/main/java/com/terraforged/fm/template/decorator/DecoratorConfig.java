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

package com.terraforged.fm.template.decorator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DecoratorConfig<T extends IWorld> {

    private final Decorator.Factory<T> factory;
    private final List<Decorator<T>> defaultDecorator;
    private final Map<ResourceLocation, List<Decorator<T>>> biomeDecorators;

    private DecoratorConfig(Decorator.Factory<T> factory, List<Decorator<T>> defaultDecorator, Map<ResourceLocation, List<Decorator<T>>> biomeDecorators) {
        this.factory = factory;
        this.defaultDecorator = defaultDecorator;
        this.biomeDecorators = biomeDecorators;
    }

    public T createBuffer(ISeedReader world) {
        return factory.wrap(world);
    }

    public List<Decorator<T>> getDecorators(ResourceLocation biome) {
        if (biome == null) {
            return defaultDecorator;
        }
        return biomeDecorators.getOrDefault(biome, defaultDecorator);
    }

    public static <T extends IWorld> DecoratorConfig<T> parse(Decorator.Factory<T> factory, JsonObject config) {
        List<Decorator<T>> defaults = Collections.emptyList();
        Map<ResourceLocation, List<Decorator<T>>> biomes = Collections.emptyMap();
        if (config != null) {
            if (config.has("default")) {
                defaults = parseDecorators(factory, config.getAsJsonObject("default"));

                for (Map.Entry<String, JsonElement> e : config.entrySet()) {
                    if (e.getKey().equals("default")) {
                        continue;
                    }

                    ResourceLocation biome = new ResourceLocation(e.getKey());
                    List<Decorator<T>> decorators = parseDecorators(factory, e.getValue().getAsJsonObject());
                    if (!decorators.isEmpty()) {
                        if (biomes.isEmpty()) {
                            biomes = new HashMap<>();
                        }
                        biomes.put(biome, decorators);
                    }
                }
            } else {
                defaults = parseDecorators(factory, config);
            }
        }
        return new DecoratorConfig<>(factory, defaults, biomes);
    }

    private static <T extends IWorld> List<Decorator<T>> parseDecorators(Decorator.Factory<T> factory, JsonObject json) {
        List<Decorator<T>> decorators = Collections.emptyList();
        for (Map.Entry<String, JsonElement> e : json.entrySet()) {
            ResourceLocation name = new ResourceLocation(e.getKey());
            Optional<Decorator<T>> decorator = factory.parse(name, e.getValue());
            if (decorator.isPresent()) {
                if (decorators.isEmpty()) {
                    decorators = new ArrayList<>(json.size());
                }
                decorators.add(decorator.get());
            }
        }
        return decorators;
    }
}
