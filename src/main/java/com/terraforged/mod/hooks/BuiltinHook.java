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

package com.terraforged.mod.hooks;

import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.util.ReflectionUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

import java.lang.invoke.MethodHandle;
import java.util.Map;

public class BuiltinHook {
    private static final MethodHandle REGISTRY_GETTER = ReflectionUtil.field(RegistryAccess.WritableRegistryAccess.class, Map.class);

    public static <T> void load(RegistryAccess.Writable writable, RegistryOps<T> ops) {
        if (ops.registryLoader().isEmpty()) return;

        var backing = getBacking(writable);
        for (var registry : CommonAPI.get().getRegistryManager().getModRegistries()) {
            backing.put(registry.key().get(), registry.copy());
        }

        for (var registry : CommonAPI.get().getRegistryManager().getModRegistries()) {
            loadRegistry(registry, ops);
        }
    }
    private static <T, E> void loadRegistry(ModRegistry<T> registry, RegistryOps<E> ops) {
        var key = registry.key().get();
        var codec = registry.codec();
        var loader = ops.registryLoader().orElseThrow();
        var result = loader.overrideRegistryFromResources(key, codec, ops.getAsJson());
        RegistryAccessUtil.printRegistryContents(result.result().orElseThrow());
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceKey<?>, Registry<?>> getBacking(RegistryAccess.Writable writable) {
        try {
            return (Map<ResourceKey<?>, Registry<?>>) REGISTRY_GETTER.invokeExact((RegistryAccess.WritableRegistryAccess) writable);
        } catch (Throwable e) {
            e.printStackTrace();
            return Map.of();
        }
    }
}
