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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.*;

public class Matcher {

    private final Set<JsonPrimitive> set;
    private final Map<String, JsonElement> map;

    Matcher(Collection<JsonPrimitive> set, Map<String, JsonElement> map) {
        this.set = set.isEmpty() ? Collections.emptySet() : new HashSet<>(set);
        this.map = map.isEmpty() ? Collections.emptyMap() : new HashMap<>(map);
    }

    public boolean complete() {
        return set.isEmpty() && map.isEmpty();
    }

    public boolean test(JsonPrimitive value) {
        return set.remove(value);
    }

    public boolean test(String key, JsonElement value) {
        JsonElement match = map.get(key);
        if (match != null && match.equals(value)) {
            map.remove(key);
            return true;
        }
        return false;
    }
}
