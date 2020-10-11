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

package com.terraforged.fm.template;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.terraforged.fm.FeatureManager;
import com.terraforged.fm.data.DataManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TemplateLoader {

    private final DataManager manager;
    private final Map<ResourceLocation, Template> cache = new HashMap<>();

    public TemplateLoader(DataManager manager) {
        this.manager = manager;
    }

    public List<Template> load(String namespace, JsonArray paths) {
        List<Template> list = new ArrayList<>();
        for (JsonElement element : paths) {
            ResourceLocation location = parsePath(namespace, element.getAsString());
            manager.forEach(location.getPath(), DataManager.NBT, (name, data) -> {
                Template template = cache.get(name);
                if (template == null) {
                    Optional<Template> instance = Template.load(data);
                    if (instance.isPresent()) {
                        FeatureManager.LOG.debug("Loading template {}", name);
                        cache.put(name, instance.get());
                        list.add(instance.get());
                    } else {
                        FeatureManager.LOG.debug("Failed to load template {}", name);
                    }
                } else {
                    list.add(template);
                }
            });
        }
        return list;
    }

    private static ResourceLocation parsePath(String namespace, String path) {
        String location = path;
        int split = location.indexOf(':');
        if (split > 0) {
            namespace = location.substring(0, split);
            location = location.substring(split + 1);
            if (!location.startsWith("structures/")) {
                location = "structures/" + location;
            }
        }
        return new ResourceLocation(namespace, location);
    }
}
