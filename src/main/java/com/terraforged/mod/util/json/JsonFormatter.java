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

package com.terraforged.mod.util.json;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Predicate;

public class JsonFormatter {
    private static final Gson GSON = new Gson();
    private static final String INDENT = "  ";
    private static final String COMPACT = "";

    private final Writer writer;
    private final JsonWriter jsonWriter;

    public JsonFormatter(Writer writer) {
        this.writer = writer;
        this.jsonWriter = new JsonWriter(writer);
        this.jsonWriter.setIndent("  ");
    }

    public void write(JsonElement json) throws IOException {
        if (json.isJsonObject()) {
            writeObject(json.getAsJsonObject());
        } else if (json.isJsonArray()) {
            writeArray(json.getAsJsonArray());
        } else if (json.isJsonPrimitive()) {
            writePrimitive(json.getAsJsonPrimitive(), false);
        }
    }

    private void writeObject(JsonObject object) throws IOException {
        var keys = getKeys(object);
        jsonWriter.beginObject();
        writeEntries(keys, object, JsonFormatter::isString);
        writeEntries(keys, object, JsonFormatter::isPrimitive);
        writeEntries(keys, object, JsonElement::isJsonArray);
        writeEntries(keys, object, JsonElement::isJsonObject);
        jsonWriter.endObject();
    }

    private void writeEntries(String[] keys, JsonObject object, Predicate<JsonElement> predicate) throws IOException {
        for (var key : keys) {
            var value = object.get(key);
            if (predicate.test(value)) {
                jsonWriter.name(key);
                write(value);
            }
        }
    }

    private void writeArray(JsonArray array) throws IOException {
        if (isCompactable(array)) {
            writeCompact(array);
        } else {
            writeNormal(array);
        }
    }

    private void writeCompact(JsonArray array) throws IOException {
        jsonWriter.beginArray();
        jsonWriter.setIndent(COMPACT);

        for (int i = 0; i < array.size(); i++) {
            if (i > 0) {
                writer.write(", ");
            }
            writePrimitive(array.get(i).getAsJsonPrimitive(), true);
        }

        jsonWriter.endArray();
        jsonWriter.setIndent(INDENT);
    }

    private void writeNormal(JsonArray array) throws IOException {
        jsonWriter.beginArray();
        for (int i = 0; i < array.size(); i++) {
            write(array.get(i));
        }
        jsonWriter.endArray();
    }

    private void writePrimitive(JsonPrimitive json, boolean direct) throws IOException {
        if (json.isNumber()) {
            writeNumber(json, direct);
        } else if (json.isBoolean()) {
            writeBool(json, direct);
        } else if (json.isString()) {
            writeString(json, direct);
        }
    }

    private void writeNumber(JsonPrimitive json, boolean direct) throws IOException {
        long l = json.getAsLong();
        double d = json.getAsDouble();
        if (l == d) {
            if (direct) {
                writer.write(String.valueOf(l));
            } else {
                jsonWriter.value(l);
            }
        } else {
            if (direct) {
                writer.write(String.valueOf(trimDouble(d)));
            } else {
                jsonWriter.value(trimDouble(d));
            }
        }
    }

    private void writeBool(JsonPrimitive json, boolean direct) throws IOException {
        if (direct) {
            GSON.toJson(json, writer);
        } else {
            jsonWriter.value(json.getAsBoolean());
        }
    }

    private void writeString(JsonPrimitive json, boolean direct) throws IOException {
        if (direct) {
            GSON.toJson(json, writer);
        } else {
            jsonWriter.value(json.getAsString());
        }
    }

    private static double trimDouble(double value) {
        int factor = 1000;
        while (value * factor < 1) {
            factor *= 10;
        }
        return Math.round(value * factor) / (double) factor;
    }

    private static boolean isString(JsonElement json) {
        return json.isJsonPrimitive() && json.getAsJsonPrimitive().isString();
    }

    private static boolean isPrimitive(JsonElement json) {
        return json.isJsonPrimitive() && !json.getAsJsonPrimitive().isString();
    }

    private static boolean isCompactable(JsonArray array) {
        int size = array.size();
        if (size == 0) return false;

        var first = array.get(0);
        if (!first.isJsonPrimitive()) return false;

        var prim = first.getAsJsonPrimitive();
        return !prim.isString() || size < 3;
    }

    private static String[] getKeys(JsonObject json) {
        return json.keySet().stream().sorted().toArray(String[]::new);
    }

    public static void apply(JsonElement jsonElement, Writer writer) throws IOException {
        new JsonFormatter(writer).write(jsonElement);
    }
}
