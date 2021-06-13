/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.featuremanager.template.type.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.terraforged.mod.featuremanager.template.decorator.Decorator;
import com.terraforged.mod.featuremanager.util.codec.CodecHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class TreeDecoratorFactory implements Decorator.Factory<TreeDecoratorBuffer> {

    @Override
    public TreeDecoratorBuffer wrap(ISeedReader world) {
        return new TreeDecoratorBuffer(world);
    }

    @Override
    public Optional<Decorator<TreeDecoratorBuffer>> parse(ResourceLocation name, JsonElement config) {
        TreeDecoratorType<?> type = ForgeRegistries.TREE_DECORATOR_TYPES.getValue(name);
        if (type == null) {
            return Optional.empty();
        }

        JsonElement modified = getModifiedConfig(config);
        net.minecraft.world.gen.treedecorator.TreeDecorator decorator = decode(type, config);
        net.minecraft.world.gen.treedecorator.TreeDecorator modifiedDecorator = decorator;

        if (modified != config) {
            modifiedDecorator = decode(type, modified);
        }

        return Optional.of(new TreeDecorator(decorator, modifiedDecorator));
    }

    private static net.minecraft.world.gen.treedecorator.TreeDecorator decode(TreeDecoratorType<?> type, JsonElement config) {
        return CodecHelper.treeDecorator(type, config, JsonOps.INSTANCE);
    }

    private static JsonElement getModifiedConfig(JsonElement config) {
        if (!config.isJsonObject()) {
            return config;
        }

        JsonObject object = config.getAsJsonObject();
        if (object.has("probability") && object.has("modified_probability")) {
            double probability = object.get("modified_probability").getAsDouble();
            JsonObject result = new JsonObject();
            result.addProperty("probability", probability);
            return result;
        }

        return config;
    }
}
