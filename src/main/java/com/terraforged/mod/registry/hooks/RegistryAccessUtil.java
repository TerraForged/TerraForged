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

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.ModRegistries;
import com.terraforged.mod.util.ReflectionUtil;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Optional;

public class RegistryAccessUtil {
    private static final MethodHandle REGISTRY_ACCESS_GETTER = ReflectionUtil.field(RegistryReadOps.class, RegistryAccess.class);
    private static final MethodHandle REGISTRY_HOLDER_MAP = ReflectionUtil.field(RegistryAccess.RegistryHolder.class, Map.class);

    public static Optional<RegistryAccess> getRegistryAccess(DynamicOps<?> ops) {
        if (!(ops instanceof RegistryReadOps)) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(getRegistryAccess((RegistryReadOps<?>) ops));
        } catch (Throwable t) {
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static RegistryAccess getRegistryAccess(RegistryReadOps<?> ops) {
        try {
            return (RegistryAccess) REGISTRY_ACCESS_GETTER.invokeExact(ops);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<ResourceKey<? extends Registry<?>>, MappedRegistry<?>> getHolderMap(RegistryAccess holder) {
        return getHolderMap((RegistryAccess.RegistryHolder) holder);
    }

    @SuppressWarnings("unchecked")
    public static Map<ResourceKey<? extends Registry<?>>, MappedRegistry<?>> getHolderMap(RegistryAccess.RegistryHolder holder) {
        try {
            return (Map<ResourceKey<? extends Registry<?>>, MappedRegistry<?>>) REGISTRY_HOLDER_MAP.invokeExact(holder);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> MappedRegistry<T> copyRegistry(Registry<T> src) {
        var registry = new MappedRegistry<>(src.key(), Lifecycle.stable());
        for (var entry : src.entrySet()) {
            registry.register(entry.getKey(), entry.getValue(), Lifecycle.stable());
        }
        return registry;
    }

    @SuppressWarnings("ConstantConditions")
    public static void extendRegistryHolder(RegistryAccess src, RegistryAccess dest) {
        var srcMap = RegistryAccessUtil.getHolderMap((RegistryAccess.RegistryHolder) src);
        var destMap = RegistryAccessUtil.getHolderMap((RegistryAccess.RegistryHolder) dest);

        // Copy vanilla builtins
        destMap.putAll(srcMap);

        // Copy mod builtins
        for (var holder : ModRegistries.getHolders()) {
            var registry = copyRegistry(holder.registry());
            destMap.put(holder.key(), registry);
        }
    }

    public static void printRegistryContents(Registry<?> registry) {
        if (!Environment.DEBUGGING) return;

        TerraForged.LOG.info(" - Registry: {}, Size: {}", registry.key().location(), registry.size());
        for (var entry : registry.entrySet()) {
            TerraForged.LOG.info("  - {}", entry.getKey().location());
        }
    }
}
