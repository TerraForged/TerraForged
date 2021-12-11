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

package com.terraforged.mod.registry;

import com.google.gson.JsonParseException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.ReflectionUtils;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RegistryAccessUtil {
    private static final MethodHandle REGISTRY_ACCESS_GETTER = ReflectionUtils.field(RegistryReadOps.class, RegistryAccess.class);

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

    public static Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> extend(
            Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> map) {

        TerraForged.LOG.info("Extending world-gen registries");
        var copy = new HashMap<ResourceKey<? extends Registry<?>>, MappedRegistry<?>>(map);
        for (var holder : ModRegistries.getHolders()) {
            copy.put(holder.key(), new MappedRegistry<>(holder.key(), Lifecycle.stable()));
        }

        return copy;
    }

    public static void inject(RegistryAccess.RegistryHolder builtin, RegistryResourceAccess.InMemoryStorage storage) {
        TerraForged.LOG.info("Injecting world-gen registry defaults");
        var context = getExtendedContext(builtin);
        for (var holder : ModRegistries.getHolders()) {
            injectRegistry(holder, context, storage);
        }
    }

    public static void load(RegistryAccess access, RegistryReadOps<?> ops) {
        TerraForged.LOG.info("Loading world-gen registry content from data");
        for (var holder : ModRegistries.getHolders()) {
            loadData(holder, access, ops);
        }
    }

    private static <T> void injectRegistry(ModRegistries.Holder<T> holder,
                                           RegistryAccess.RegistryHolder context,
                                           RegistryResourceAccess.InMemoryStorage storage) {
        var registry = holder.registry();
        for (var entry : registry.entrySet()) {
            var value = entry.getValue();
            storage.add(context, entry.getKey(), holder.direct(), registry.getId(value), value, registry.lifecycle(value));
        }

        printRegistryContents(registry);
    }

    private static RegistryAccess.RegistryHolder getExtendedContext(RegistryAccess.RegistryHolder builtin) {
        var extended = new RegistryAccess.RegistryHolder();
        for (var data : RegistryAccess.knownRegistries()) {
            copy(data.key(), builtin::registryOrThrow, extended::registryOrThrow);
        }
        for (var holder : ModRegistries.getHolders()) {
            copy(holder, extended::registryOrThrow);
        }
        return extended;
    }

    private static  <T> void loadData(ModRegistries.Holder<T> holder, RegistryAccess access, RegistryReadOps<?> ops) {
        var registry = access.ownedRegistry(holder.key());
        if (registry.isEmpty()) return;

        var mappedRegistry = (MappedRegistry<T>) registry.get();
        var result = ops.decodeElements(mappedRegistry, holder.key(), holder.direct());

        printRegistryContents(mappedRegistry);

        result.error().ifPresent((partialResult) -> {
            throw new JsonParseException("Error loading registry data: " + partialResult.message());
        });
    }

    private static <T> void copy(ModRegistries.Holder<T> holder, Function<ResourceKey<? extends Registry<T>>, Registry<T>> dest) {
        copy(holder.key(), k -> holder.registry(), dest);
    }

    private static <T> void copy(ResourceKey<? extends Registry<T>> key,
                                 Function<ResourceKey<? extends Registry<T>>, Registry<T>> source,
                                 Function<ResourceKey<? extends Registry<T>>, Registry<T>> dest) {

        var input = source.apply(key);
        var output = (WritableRegistry<T>) dest.apply(key);
        for (var entry : input.entrySet()) {
            output.register(entry.getKey(), entry.getValue(), input.lifecycle(entry.getValue()));
        }
    }

    private static void printRegistryContents(Registry<?> registry) {
        if (!Environment.DEBUGGING) return;

        TerraForged.LOG.info(" - Registry: {}, Size: {}", registry.key().location(), registry.size());
        for (var entry : registry.entrySet()) {
            TerraForged.LOG.info("  - {}", entry.getKey().location());
        }
    }
}
