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

package com.terraforged.mod.registry.hooks;

import com.google.gson.JsonParseException;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.ModRegistries;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryReadOps;

public class DataLoadHook {
    public static void loadData(RegistryAccess access, RegistryReadOps<?> ops) {
        TerraForged.LOG.info("Loading world-gen registry content from data");

        for (var holder : ModRegistries.getHolders()) {
            try {
                loadData(holder, access, ops);
            } catch (Throwable t) {
                throw new Error("Failed to load holder: " + holder.key(), t);
            }
        }
    }

    private static  <T> void loadData(ModRegistries.Holder<T> holder, RegistryAccess access, RegistryReadOps<?> ops) {
        var registry = access.ownedRegistry(holder.key());
        if (registry.isEmpty()) return;

        var mappedRegistry = (MappedRegistry<T>) registry.get();
        var result = ops.decodeElements(mappedRegistry, holder.key(), holder.direct());

        RegistryAccessUtil.printRegistryContents(mappedRegistry);

        result.error().ifPresent((partialResult) -> {
            throw new JsonParseException("Error loading registry data: " + partialResult.message());
        });
    }
}
