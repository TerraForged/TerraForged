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

package com.terraforged.fm.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Set;

public class TagLoader {

    static <T extends IForgeRegistryEntry<T>> void loadTag(JsonObject object, ITag.INamedTag<T> tag, IForgeRegistry<T> registry, Set<T> set) {
        if (object.has("replace") && object.get("replace").getAsBoolean()) {
            set.clear();
        }

        if (object.has("values")) {
            for (JsonElement element : object.get("values").getAsJsonArray()) {
                ResourceLocation resourceName = ResourceLocation.tryCreate(element.getAsString());
                if (resourceName == null) {
                    continue;
                }

                T value = registry.getValue(resourceName);
                if (value != null) {
                    set.add(value);
                }
            }
        }
    }
}
