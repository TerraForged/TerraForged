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

package com.terraforged.fm.transformer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.terraforged.fm.FeatureSerializer;
import net.minecraft.world.gen.GenerationStage;

import java.util.Map;
import java.util.Optional;

public class FeatureParser {

    public static Optional<FeatureReplacer> parseReplacer(JsonObject root) {
        if (root.has("replace")) {
            return FeatureSerializer.deserialize(root.get("replace")).map(FeatureReplacer::of);
        }
        return Optional.empty();
    }

    public static Optional<FeatureInjector> parseInjector(JsonObject root) {
        if (root.has("before")) {
            return FeatureSerializer.deserialize(root.get("before"))
                    .map(feature -> new FeatureInjector(feature, InjectionPosition.BEFORE));
        }
        if (root.has("after")) {
            return FeatureSerializer.deserialize(root.get("after"))
                    .map(feature -> new FeatureInjector(feature, InjectionPosition.AFTER));
        }
        return Optional.empty();
    }

    public static Optional<FeatureAppender> parseAppender(JsonObject root) {
        if (root.has("stage")) {
            GenerationStage.Decoration stage = GenerationStage.Decoration.valueOf(root.get("stage").getAsString());
            if (root.has("prepend")) {
                return FeatureSerializer.deserialize(root.get("prepend"))
                        .map(feature -> new FeatureAppender(feature, InjectionPosition.HEAD, stage));
            }
            if (root.has("append")) {
                return FeatureSerializer.deserialize(root.get("append"))
                        .map(feature -> new FeatureAppender(feature, InjectionPosition.TAIL, stage));
            }
        }
        return Optional.empty();
    }

    public static Optional<FeatureTransformer> parseTransformer(JsonObject root) {
        if (root.has("transform")) {
            return parseTransform(root.get("transform"));
        }
        return Optional.empty();
    }

    private static Optional<FeatureTransformer> parseTransform(JsonElement element) {
        if (!element.isJsonObject()) {
            return Optional.empty();
        }
        FeatureTransformer.Builder builder = FeatureTransformer.builder();
        for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
            if (e.getValue().isJsonPrimitive()) {
                JsonPrimitive key = keyToPrimitive(e.getKey(), e.getValue().getAsJsonPrimitive());
                if (key == null) {
                    return Optional.empty();
                }
                builder.value(key, e.getValue().getAsJsonPrimitive());
            } else {
                builder.key(e.getKey(), e.getValue());
            }
        }
        return Optional.of(builder.build());
    }

    private static JsonPrimitive keyToPrimitive(String key, JsonPrimitive value) {
        if (value.isString()) {
            return new JsonPrimitive(key);
        }
        if (value.isNumber()) {
            return new JsonPrimitive(new JsonPrimitive(key).getAsNumber());
        }
        if (value.isBoolean()) {
            return new JsonPrimitive(new JsonPrimitive(key).getAsBoolean());
        }
        return null;
    }
}
