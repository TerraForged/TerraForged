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
import com.google.gson.JsonPrimitive;

import java.util.*;

public class Rule {

    private final Collection<JsonPrimitive> values;

    public Rule(Collection<JsonPrimitive> values) {
        this.values = values;
    }

    public JsonElement toJson() {
        if (values.size() == 1) {
            for (JsonPrimitive primitive : values) {
                return primitive;
            }
        }
        JsonArray array = new JsonArray();
        for (JsonPrimitive primitive : values) {
            array.add(primitive);
        }
        return array;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "values=" + values +
                '}';
    }

    public Matcher createMatcher() {
        return new Matcher(values);
    }

    public static List<Rule> parseRules(JsonElement element) {
        List<Rule> rules = new LinkedList<>();
        if (element.isJsonPrimitive()) {
            rules.add(new Rule(Collections.singleton(element.getAsJsonPrimitive())));
        } else if (element.isJsonArray()) {
            boolean arrays = true;
            boolean primitive = true;
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive()) {
                    arrays = false;
                    continue;
                }
                if (e.isJsonArray()) {
                    primitive = false;
                    continue;
                }
                return Collections.emptyList();
            }
            if (primitive) {
                Collection<JsonPrimitive> primitives = getPrimitives(element.getAsJsonArray());
                rules.add(new Rule(primitives));
            } else if (arrays) {
                for (JsonElement e : element.getAsJsonArray()) {
                    Collection<JsonPrimitive> primitives = getPrimitives(e.getAsJsonArray());
                    rules.add(new Rule(primitives));
                }
            }
        }
        return rules;
    }

    private static Collection<JsonPrimitive> getPrimitives(JsonArray array) {
        Set<JsonPrimitive> set = new HashSet<>();
        for (JsonElement e : array) {
            if (!e.isJsonPrimitive()) {
                return Collections.emptyList();
            }
            set.add(e.getAsJsonPrimitive());
        }
        return set;
    }
}
