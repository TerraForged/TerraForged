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

import com.terraforged.fm.FeatureManager;
import com.terraforged.fm.data.DataManager;
import com.terraforged.fm.template.feature.TemplateFeatureConfig;
import com.terraforged.fm.template.type.FeatureTypes;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashMap;
import java.util.Map;

public class TemplateManager {

    private static final Marker marker = MarkerManager.getMarker("TEMPLATES");

    private static final TemplateManager instance = new TemplateManager();

    private final Map<ResourceLocation, TemplateFeatureConfig> templates = new HashMap<>();

    public synchronized TemplateFeatureConfig getTemplateConfig(ResourceLocation name) {
        return templates.getOrDefault(name, TemplateFeatureConfig.NONE);
    }

    public synchronized void load(DataManager dataManager) {
        clear();
        FeatureTypes.clearFeatures();
        TemplateLoader loader = new TemplateLoader(dataManager);
        dataManager.forEachJson("templates", (location, data) -> {
            if (data.isJsonObject()) {
                TemplateFeatureConfig instance = TemplateFeatureConfig.parse(loader, data.getAsJsonObject());
                templates.put(instance.name, instance);
                FeatureManager.LOG.debug(marker, " Loaded template config: {}, size:{}", location, instance.templates.size());
            } else {
                FeatureManager.LOG.error(marker, " Failed to load template config: {}", location);
            }
        });
    }

    public synchronized void clear() {
        templates.clear();
    }

    public static TemplateManager getInstance() {
        return instance;
    }
}
