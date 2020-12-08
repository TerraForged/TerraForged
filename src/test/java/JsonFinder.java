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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;

import java.util.Map;
import java.util.function.Predicate;

public class JsonFinder {

    public static void apply(JsonElement json, Map<JsonElement, String> featureIds) {
        find(json, "features", JsonElement::isJsonArray, parent -> {
            JsonArray features = parent.getAsJsonArray("features");
            JsonArray replacement = new JsonArray();

            for (JsonElement element : features) {
                String id = featureIds.get(element);
                if (id != null) {
                    replacement.add(id);
                }
            }

            parent.add("features", replacement);
            return false;
        });
    }

    /**
     * @param element the json element to search within
     * @param key     the key to find in the element
     * @param type    the expected type for the value assigned to the given key
     * @param action  applies an action on the json object containing the matching key/value pair. return true to stop
     *               searching through the json tree (ie findFirst) or false to continue (findAll).
     */
    public static boolean find(JsonElement element, String key, Predicate<JsonElement> type, Predicate<JsonObject> action) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has(key) && type.test(object.get(key))) {
                if (action.test(object)) {
                    return true;
                }
            }
            for (Map.Entry<String, JsonElement> e : object.entrySet()) {
                if (find(e.getValue(), key, type, action)) {
                    return true;
                }
            }
            return false;
        } else if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                if (find(e, key, type, action)) {
                    return true;
                }
            }
        }
        return false;
    }
}
