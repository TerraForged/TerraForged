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

package com.terraforged.fm.matcher.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Optional;

public class FeatureMatcherParser {

    public static Optional<FeatureMatcher> parse(JsonObject root) {
        if (root.has("match")) {
            return parse(root.get("match"));
        }
        return Optional.of(FeatureMatcher.ANY);
    }

    private static Optional<FeatureMatcher> parse(JsonElement element) {
        if (element.isJsonPrimitive()) {
            Object arg = parseArg(element.getAsJsonPrimitive());
            if (arg == null) {
                return Optional.empty();
            }
            return Optional.of(FeatureMatcher.of(arg));
        }

        if (element.isJsonArray()) {
            FeatureMatcher.Builder builder = FeatureMatcher.builder();
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive()) {
                    Object arg = parseArg(e.getAsJsonPrimitive());
                    if (arg == null) {
                        return Optional.empty();
                    }
                    builder.or(arg);
                } else if (e.isJsonArray()) {
                    if (!parseRule(e.getAsJsonArray(), builder.newRule())) {
                        return Optional.empty();
                    }
                } else {
                    // invalid syntax
                    return Optional.empty();
                }
            }
            return Optional.of(builder.build());
        }

        return Optional.empty();
    }

    private static boolean parseRule(JsonArray array, FeatureMatcher.Builder builder) {
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive()) {
                return false;
            }
            Object arg = parseArg(element.getAsJsonPrimitive());
            if (arg == null) {
                return false;
            }
            builder.and(arg);
        }
        return true;
    }

    private static Object parseArg(JsonPrimitive primitive) {
        if (primitive.isString()) {
            return primitive.getAsString();
        }
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isNumber()) {
            return primitive.getAsNumber();
        }
        return null;
    }
}
