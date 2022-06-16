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
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.DataRegistry;
import com.terraforged.mod.util.ReflectionUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.lang.invoke.MethodHandle;
import java.util.Map;

public class BuiltinHook {
    private static final MethodHandle REGISTRY_GETTER = ReflectionUtil.field(RegistryAccess.WritableRegistryAccess.class, Map.class);
    private static final MethodHandle READ_CACHE_GETTER = ReflectionUtil.field(RegistryLoader.class, Map.class);

    public static <T> void load(RegistryAccess.Writable writable, RegistryOps<T> ops) {
        if (ops.registryLoader().isEmpty()) return;

        var backing = getBacking(writable);
        for (var registry : CommonAPI.get().getRegistryManager().getInjectedRegistries()) {
            backing.putIfAbsent(registry.key().get(), registry.copy());
        }

        for (var registry : CommonAPI.get().getRegistryManager().getInjectedRegistries()) {
            loadRegistry(registry, ops);
        }

        // Note: need to reload the preset registry after injecting datapack
        // content so that generators have access to the additional data!
        reloadPresetRegistry(writable, ops);
    }

    private static <T, E> void loadRegistry(DataRegistry<T> registry, RegistryOps<E> ops) {
        var key = registry.key().get();
        var codec = registry.codec();
        var loader = ops.registryLoader().orElseThrow();
        var result = loader.overrideRegistryFromResources(key, codec, ops.getAsJson());
        RegistryAccessUtil.printRegistryContents(result.result().orElseThrow());
    }

    private static <T> void reloadPresetRegistry(RegistryAccess.Writable writable, RegistryOps<T> ops) {
        var loader = ops.registryLoader().orElseThrow().loader();

        // Clear cached world-presets so that they're read fully from
        // json, allowing access to the newly injected data.
        clearReadCache(loader, Registry.WORLD_PRESET_REGISTRY);

        var registry = writable.ownedWritableRegistryOrThrow(Registry.WORLD_PRESET_REGISTRY);
        loader.overrideRegistryFromResources(registry, Registry.WORLD_PRESET_REGISTRY, WorldPreset.DIRECT_CODEC, ops.getAsJson());

        TerraForged.LOG.info("Reloaded world-preset registry");
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceKey<?>, Registry<?>> getBacking(RegistryAccess.Writable writable) {
        try {
            return (Map<ResourceKey<?>, Registry<?>>) REGISTRY_GETTER.invokeExact((RegistryAccess.WritableRegistryAccess) writable);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void clearReadCache(RegistryLoader loader, ResourceKey<?> registry) {
        try {
            var map = (Map<?, ?>) READ_CACHE_GETTER.invokeExact(loader);
            map.remove(registry);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
