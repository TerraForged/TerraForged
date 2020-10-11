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

package com.terraforged.fm.template.feature;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.fm.template.PasteConfig;
import com.terraforged.fm.template.Template;
import com.terraforged.fm.template.TemplateLoader;
import com.terraforged.fm.template.TemplateManager;
import com.terraforged.fm.template.decorator.DecoratorConfig;
import com.terraforged.fm.template.type.FeatureType;
import com.terraforged.fm.template.type.FeatureTypes;
import com.terraforged.fm.util.Json;
import com.terraforged.fm.util.codec.Codecs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.Collections;
import java.util.List;

public class TemplateFeatureConfig implements IFeatureConfig {

    public static final TemplateFeatureConfig NONE = new TemplateFeatureConfig();
    public static final Codec<TemplateFeatureConfig> CODEC = Codecs.create(
            TemplateFeatureConfig::serialize,
            TemplateFeatureConfig::deserialize
    );

    public final ResourceLocation name;
    public final FeatureType type;
    public final PasteConfig paste;
    public final List<Template> templates;
    public final DecoratorConfig<?> decorator;

    private TemplateFeatureConfig() {
        name = new ResourceLocation("fm", "none");
        type = FeatureTypes.ANY;
        paste = PasteConfig.DEFAULT;
        templates = Collections.emptyList();
        decorator = null;
    }

    public TemplateFeatureConfig(ResourceLocation name, FeatureType type, PasteConfig paste, List<Template> templates, DecoratorConfig<?> decorator) {
        this.name = name;
        this.type = type;
        this.paste = paste;
        this.templates = templates;
        this.decorator = decorator;
        type.register(name);
    }

    public static <T> Dynamic<T> serialize(TemplateFeatureConfig config, DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("template"), ops.createString(config.name.toString())
        )));
    }

    public static <T> TemplateFeatureConfig deserialize(Dynamic<T> dynamic) {
        ResourceLocation name = new ResourceLocation(dynamic.get("template").asString(""));
        return TemplateManager.getInstance().getTemplateConfig(name);
    }

    public static TemplateFeatureConfig parse(TemplateLoader loader, JsonObject root) {
        ResourceLocation name = new ResourceLocation(Json.getString("name", root, ""));
        FeatureType type = FeatureTypes.getType(Json.getString("type", root, ""));
        PasteConfig paste = PasteConfig.parse(root.getAsJsonObject("config"));
        DecoratorConfig<?> decorator = DecoratorConfig.parse(type.getDecorator(), root.getAsJsonObject("decorators"));
        List<Template> templates = loader.load(name.getNamespace(), root.getAsJsonArray("paths"));
        return new TemplateFeatureConfig(name, type, paste, templates, decorator);
    }
}
