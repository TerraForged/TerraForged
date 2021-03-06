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

package com.terraforged.mod.featuremanager.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.mod.featuremanager.util.codec.CodecException;

import java.util.function.Function;

public class Json {

    public static String getString(String path, JsonObject owner) throws CodecException {
        String s = get(path, owner, JsonElement::getAsString, null);
        if (s == null) {
            throw CodecException.of("Missing string entry for %s", path);
        }
        return s;
    }

    public static int getInt(String path, JsonObject owner) throws CodecException {
        Number n = get(path, owner, JsonElement::getAsNumber, null);
        if (n == null) {
            throw CodecException.of("Missing int entry value for %s", path);
        }
        return n.intValue();
    }

    public static float getFloat(String path, JsonObject owner) throws CodecException {
        Number n = get(path, owner, JsonElement::getAsNumber, null);
        if (n == null) {
            throw CodecException.of("Missing float entry value for %s", path);
        }
        return n.floatValue();
    }

    public static boolean getBool(String path, JsonObject owner) throws CodecException {
        if (owner.has(path)) {
            return getBool(path, owner, false);
        }
        throw CodecException.of("Missing boolean entry value for %s", path);
    }

    public static String getString(String path, JsonObject owner, String def) {
        return get(path, owner, JsonElement::getAsString, def);
    }

    public static int getInt(String path, JsonObject owner, int def) {
        return get(path, owner, JsonElement::getAsInt, def);
    }

    public static boolean getBool(String path, JsonObject owner, boolean def) {
        return get(path, owner, JsonElement::getAsBoolean, def);
    }

    public static float getFloat(String path, JsonObject owner, float def) {
        return get(path, owner, JsonElement::getAsFloat, def);
    }

    public static <T> T get(String path, JsonObject owner, Function<JsonElement, T> func, T def) {
        if (owner == null) {
            return def;
        }
        JsonElement child = owner.get(path);
        if (child == null) {
            return def;
        }
        return func.apply(child);
    }
}
