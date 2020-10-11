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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.terraforged.fm.GameContext;
import com.terraforged.fm.matcher.feature.FeatureMatcher;
import com.terraforged.fm.modifier.Jsonifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FeatureTransformer implements Function<JsonElement, JsonElement>, Jsonifiable {

    public static final FeatureTransformer NONE = FeatureTransformer.builder().build();

    private final boolean hasTransformations;
    private final Map<String, JsonElement> keyTransformers;
    private final Map<JsonPrimitive, JsonPrimitive> valueTransformers;

    private FeatureTransformer(Builder builder) {
        this.keyTransformers = builder.keyTransformers;
        this.valueTransformers = builder.valueTransformers;
        this.hasTransformations = !keyTransformers.isEmpty() || !valueTransformers.isEmpty();
        builder.keyTransformers = Collections.emptyMap();
        builder.valueTransformers = Collections.emptyMap();
    }

    @Override
    public String getType() {
        return "transform";
    }

    @Override
    public JsonElement toJson(GameContext context) {
        JsonObject root = new JsonObject();
        for (Map.Entry<JsonPrimitive, JsonPrimitive> entry : valueTransformers.entrySet()) {
            root.add(entry.getKey().getAsString(), entry.getValue());
        }
        for (Map.Entry<String, JsonElement> entry : keyTransformers.entrySet()) {
            root.add(entry.getKey(), entry.getValue());
        }
        return root;
    }

    @Override
    public JsonElement apply(JsonElement element) {
        if (hasTransformations) {
            if (element.isJsonArray()) {
                return transformArray(element.getAsJsonArray());
            }
            if (element.isJsonObject()) {
                return transformObject(element.getAsJsonObject());
            }
            if (element.isJsonPrimitive()) {
                return transformValue(element.getAsJsonPrimitive());
            }
        }
        return element;
    }

    private JsonPrimitive transformValue(JsonPrimitive primitive) {
        return valueTransformers.getOrDefault(primitive, primitive);
    }

    private JsonArray transformArray(JsonArray source) {
        JsonArray dest = new JsonArray();
        for (JsonElement element : source) {
            dest.add(apply(element));
        }
        return dest;
    }

    private JsonObject transformObject(JsonObject source) {
        JsonObject dest = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            JsonElement result = transformEntry(entry.getKey(), entry.getValue());
            dest.add(entry.getKey(), result);
        }
        return dest;
    }

    private JsonElement transformEntry(String key, JsonElement value) {
        JsonElement keyResult = keyTransformers.get(key);
        if (keyResult != null) {
            return keyResult;
        }
        return apply(value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static FeatureTransformer key(String key, Object value) {
        return builder().key(key, FeatureMatcher.arg(value)).build();
    }

    public static <T> FeatureTransformer replace(T find, T replace) {
        JsonElement f = FeatureMatcher.arg(find);
        JsonElement r = FeatureMatcher.arg(replace);
        if (f.isJsonPrimitive() && r.isJsonPrimitive()) {
            return builder().value(f.getAsJsonPrimitive(), r.getAsJsonPrimitive()).build();
        }
        return NONE;
    }

    public static class Builder {

        private Map<String, JsonElement> keyTransformers = Collections.emptyMap();
        private Map<JsonPrimitive, JsonPrimitive> valueTransformers = Collections.emptyMap();

        public Builder key(String key, boolean value) {
            return key(key, new JsonPrimitive(value));
        }

        public Builder key(String key, Number value) {
            return key(key, new JsonPrimitive(value));
        }

        public Builder key(String key, String value) {
            return key(key, new JsonPrimitive(value));
        }

        public Builder key(String key, JsonElement value) {
            if (keyTransformers.isEmpty()) {
                keyTransformers = new HashMap<>();
            }
            keyTransformers.put(key, value);
            return this;
        }

        public Builder value(boolean find, boolean replace) {
            return value(new JsonPrimitive(find), new JsonPrimitive(replace));
        }

        public Builder value(Number find, Number replace) {
            return value(new JsonPrimitive(find), new JsonPrimitive(replace));
        }

        public Builder value(String find, String replace) {
            return value(new JsonPrimitive(find), new JsonPrimitive(replace));
        }

        public Builder value(JsonPrimitive find, JsonPrimitive replace) {
            if (valueTransformers.isEmpty()) {
                valueTransformers = new HashMap<>();
            }
            valueTransformers.put(find, replace);
            return this;
        }

        public FeatureTransformer build() {
            return new FeatureTransformer(this);
        }
    }
}
