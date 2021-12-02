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

package com.terraforged.mod.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.spec.DataSpecs;
import com.terraforged.cereal.value.DataList;
import com.terraforged.cereal.value.DataObject;
import com.terraforged.cereal.value.DataValue;

public class DataUtil {
    public static final String TYPE_KEY = "type";

    public static DataValue toData(JsonElement json) {
        return toData(json, TYPE_KEY);
    }

    public static DataValue toData(JsonElement json, String typeKey) {
        if (json.isJsonObject()) {
            var jsonObject = json.getAsJsonObject();
            var name = jsonObject.get(typeKey);
            var object = new DataObject(name == null ? "" : name.getAsString());

            for (var entry : json.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals(typeKey)) continue;

                object.add(entry.getKey(), toData(entry.getValue(), typeKey));
            }

            return object;
        }

        if (json.isJsonArray()) {
            var array = new DataList();

            for (var entry : json.getAsJsonArray()) {
                array.add(toData(entry, typeKey));
            }

            return array;
        }

        if (json.isJsonPrimitive()) {
            var prim = json.getAsJsonPrimitive();

            if (prim.isString()) return DataValue.of(prim.getAsString());
            if (prim.isNumber()) return DataValue.of(prim.getAsNumber());
            if (prim.isBoolean()) return DataValue.of(prim.getAsBoolean());
        }

        throw new Error("Unsupported data type: " + json);
    }

    public static JsonElement toJson(DataValue value) {
        return toJson(value, TYPE_KEY);
    }

    public static JsonElement toJson(DataValue value, String typeKey) {
        if (value.isObj()) {
            JsonObject object = new JsonObject();

            if (!value.asObj().getType().isEmpty()) {
                object.addProperty(typeKey, value.asObj().getType());
            }

            for (var entry : value.asObj()) {
                object.add(entry.getKey(), toJson(entry.getValue(), typeKey));
            }

            return object;
        }

        if (value.isList()) {
            JsonArray array = new JsonArray();

            for (var element : value.asList()) {
                array.add(toJson(element, typeKey));
            }

            return array;
        }

        return JsonParser.parseString(value.asString());
    }

    public static <T, V extends T> void registerSub(Class<T> type, DataSpec<V> spec) {
        DataSpecs.register(spec);
        DataSpecs.registerSub(type, spec);
    }
}
