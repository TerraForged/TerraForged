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

package com.terraforged.mod.featuremanager.template.feature;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.featuremanager.template.PasteConfig;
import com.terraforged.mod.featuremanager.template.template.Template;
import com.terraforged.mod.featuremanager.template.template.TemplateLoader;
import com.terraforged.mod.featuremanager.template.template.TemplateManager;
import com.terraforged.mod.featuremanager.template.decorator.DecoratorConfig;
import com.terraforged.mod.featuremanager.template.type.FeatureType;
import com.terraforged.mod.featuremanager.template.type.FeatureTypes;
import com.terraforged.mod.featuremanager.util.Json;
import com.terraforged.mod.featuremanager.util.codec.CodecException;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.io.IOException;
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
        ResourceLocation name = Codecs.getResult(dynamic.get("template").asString())
                .map(ResourceLocation::tryCreate)
                .orElseThrow(CodecException.get("Failed to load template entry"));

        TemplateFeatureConfig config = TemplateManager.getInstance().getTemplateConfig(name);
        if (config == TemplateFeatureConfig.NONE) {
            throw CodecException.of("Failed to load config entry for %s", name);
        }
        
        return config;
    }

    public static TemplateFeatureConfig parse(TemplateLoader loader, JsonObject root) throws IOException {
        try {
            ResourceLocation name = new ResourceLocation(Json.getString("name", root));
            FeatureType type = FeatureTypes.getType(Json.getString("type", root));
            PasteConfig paste = PasteConfig.parse(root.getAsJsonObject("config"));
            DecoratorConfig<?> decorator = DecoratorConfig.parse(type.getDecorator(), root.getAsJsonObject("decorators"));
            List<Template> templates = loader.load(name.getNamespace(), root.getAsJsonArray("paths"));
            return new TemplateFeatureConfig(name, type, paste, templates, decorator);
        } catch (CodecException e) {
            throw new IOException(e);
        }
    }
}
