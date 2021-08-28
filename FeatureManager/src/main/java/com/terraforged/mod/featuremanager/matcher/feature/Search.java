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

package com.terraforged.mod.featuremanager.matcher.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

public class Search {

    private final Matcher[] matchers;
    private boolean cancelled = false;

    Search(List<Rule> list) {
        matchers = list.stream().map(Rule::createMatcher).toArray(Matcher[]::new);
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isComplete() {
        if (isCancelled()) {
            return false;
        }
        for (Matcher matcher : matchers) {
            if (matcher.complete()) {
                return true;
            }
        }
        return false;
    }

    public boolean test(JsonPrimitive value) {
        boolean hasMatch = false;
        for (Matcher matcher : matchers) {
            hasMatch |= matcher.test(value);
        }
        return hasMatch;
    }

    public boolean test(String key, JsonElement value) {
        boolean hasMatch = false;
        for (Matcher matcher : matchers) {
            hasMatch |= matcher.test(key, value);
        }
        return hasMatch;
    }
}
