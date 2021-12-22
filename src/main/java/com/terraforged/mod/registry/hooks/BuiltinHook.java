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

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.ModRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryResourceAccess;

public class BuiltinHook {
    public static void injectBuiltin(RegistryAccess.RegistryHolder builtin, RegistryResourceAccess.InMemoryStorage storage) {
        TerraForged.LOG.info("Injecting world-gen registry defaults");

        var context = new RegistryAccess.RegistryHolder();
        RegistryAccessUtil.extendRegistryHolder(builtin, context);

        for (var holder : ModRegistries.getHolders()) {
            try {
                injectRegistry(holder, context, storage);
            } catch (Throwable t) {
                throw new Error("Failed to inject holder: " + holder.key(), t);
            }
        }
    }

    private static <T> void injectRegistry(ModRegistries.Holder<T> holder,
                                           RegistryAccess.RegistryHolder context,
                                           RegistryResourceAccess.InMemoryStorage storage) {
        var registry = holder.registry();
        for (var entry : registry.entrySet()) {
            var value = entry.getValue();
            try {
                storage.add(context, entry.getKey(), holder.direct(), registry.getId(value), value, registry.lifecycle(value));
            } catch (Throwable t) {
                throw new Error("Failed to inject entry: " + entry.getKey() + "=" + value, t);
            }
        }

        RegistryAccessUtil.printRegistryContents(registry);
    }
}
