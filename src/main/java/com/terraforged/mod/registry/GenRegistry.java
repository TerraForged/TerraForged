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
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.Init;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenRegistry extends Init {
    public static final GenRegistry INSTANCE = new GenRegistry();

    protected final List<Holder<?>> holders = new ArrayList<>();

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void doInit() {
        var writableRegistry = (WritableRegistry) BuiltinRegistries.REGISTRY;

        for (var holder : holders) {
            writableRegistry.register(holder.key, new MappedRegistry<>(holder.key, Lifecycle.stable()), Lifecycle.stable());
        }
    }

    public Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> addBuiltin(
            Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> map) {
        var copy = new HashMap<ResourceKey<? extends Registry<?>>, MappedRegistry<?>>(map);

        for (var holder : holders) {
            copy.put(holder.key, new MappedRegistry<>(holder.key, Lifecycle.stable()));
        }

        return copy;
    }

    public void loadData(RegistryAccess access, RegistryReadOps<?> ops) {
        TerraForged.LOG.info("Loading world-gen registry extensions:");
        for (var holder : holders) {
            loadData(holder, access, ops);
        }
    }

    protected <T> void loadData(Holder<T> holder, RegistryAccess access, RegistryReadOps<?> ops) {
        var registry = access.ownedRegistry(holder.key());
        if (registry.isEmpty()) return;

        var mappedRegistry = (MappedRegistry<T>) registry.get();
        if (mappedRegistry.size() != 0) return;

        var result = ops.decodeElements(mappedRegistry, holder.key(), holder.direct());
        TerraForged.LOG.info(" - World-gen registry extension: {} Size: {}", holder.key, mappedRegistry.size());

        result.error().ifPresent((partialResult) -> {
            throw new JsonParseException("Error loading registry data: " + partialResult.message());
        });
    }

    protected record Holder<T>(ResourceKey<Registry<T>> key, Codec<T> direct) {}

    public static <T> void register(ResourceKey<Registry<T>> key, Codec<T> codec) {
        if (INSTANCE.isDone()) {
            TerraForged.LOG.warn("Attempted to register extension after init: {}", key);
            return;
        }

        INSTANCE.holders.add(new Holder<>(key, codec));
    }

    public static void commit() {
        INSTANCE.init();
    }
}
