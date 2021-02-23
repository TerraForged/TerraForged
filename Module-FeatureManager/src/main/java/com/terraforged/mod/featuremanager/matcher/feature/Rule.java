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

package com.terraforged.mod.featuremanager.matcher.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.*;

public class Rule {

    private final Collection<JsonPrimitive> primitives;
    private final Map<String, JsonElement> mappings;

    public Rule(Collection<JsonPrimitive> values, Map<String, JsonElement> mappings) {
        this.primitives = values;
        this.mappings = mappings;
    }

    public JsonElement toJson() {
        if (primitives.size() == 1 && mappings.isEmpty()) {
            for (JsonPrimitive primitive : primitives) {
                return primitive;
            }
        }

        JsonArray array = new JsonArray();
        for (JsonPrimitive primitive : primitives) {
            array.add(primitive);
        }

        for (Map.Entry<String, JsonElement> entry : mappings.entrySet()) {
            JsonObject mapping = new JsonObject();
            mapping.addProperty("key", entry.getKey());
            mapping.add("value", entry.getValue());
            array.add(mapping);
        }

        return array;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "values=" + primitives +
                ", mappings=" + mappings +
                '}';
    }

    public Matcher createMatcher() {
        return new Matcher(primitives, mappings);
    }

    public static List<Rule> parseRules(JsonElement element) {
        List<Rule> rules = new LinkedList<>();
        if (element.isJsonPrimitive()) {
            rules.add(new Rule(Collections.singleton(element.getAsJsonPrimitive()), Collections.emptyMap()));
        } else if (element.isJsonArray()) {
            boolean multiRule = false;
            boolean primitive = false;
            boolean advanced = false;

            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive()) {
                    primitive = true;
                    continue;
                }
                if (e.isJsonObject()) {
                    advanced = true;
                    continue;
                }
                if (e.isJsonArray()) {
                    multiRule = true;
                    continue;
                }
                return Collections.emptyList();
            }

            List<JsonPrimitive> primitives = primitive ? new ArrayList<>() : Collections.emptyList();
            Map<String, JsonElement> mappings = advanced ? new HashMap<>() : Collections.emptyMap();

            if (multiRule) {
                for (JsonElement rule : element.getAsJsonArray()) {
                    loadRules(rule.getAsJsonArray(), primitives, mappings);
                }
            } else {
                loadRules(element.getAsJsonArray(), primitives, mappings);
            }

            rules.add(new Rule(primitives, mappings));
        }
        return rules;
    }

    protected static void loadRules(JsonArray array, Collection<JsonPrimitive> primitives, Map<String, JsonElement> mappings) {
        for (JsonElement e : array) {
            if (e.isJsonPrimitive()) {
                primitives.add(e.getAsJsonPrimitive());
            } else if (e.isJsonObject()) {
                JsonObject object = e.getAsJsonObject();
                if (object.has("key") && object.has("value")) {
                    mappings.put(object.get("key").getAsString(), object.get("value"));
                }
            }
        }
    }
}
