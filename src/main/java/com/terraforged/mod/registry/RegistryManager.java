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

import com.mojang.serialization.Codec;
import com.terraforged.mod.registry.key.RegistryKey;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceKey;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RegistryManager {
    public static final RegistryManager DEFAULT = new RegistryManager();

    protected final List<DataRegistry<?>> injected = new ObjectArrayList<>();
    protected final Map<RegistryKey<?>, DataRegistry<?>> registries = new IdentityHashMap<>();

    public <T> void register(RegistryKey<T> registry, ResourceKey<T> key, T value) {
        getRegistry(registry).register(key, value);
    }

    public <T> void create(RegistryKey<T> registry) {
        create(registry, null, false);
    }

    public <T> void create(RegistryKey<T> registry, Codec<T> codec) {
        create(registry, codec, true);
    }

    public <T> void create(RegistryKey<T> key, Codec<T> codec, boolean injected) {
        var registry = new DataRegistry<>(key, codec);

        registries.put(key, registry);

        if (injected) {
            this.injected.add(registry);
        }
    }

    public Iterable<DataRegistry<?>> getRegistries() {
        return registries.values();
    }

    public Iterable<DataRegistry<?>> getInjectedRegistries() {
        return injected;
    }

    public <T> DataRegistry<T> getRegistry(RegistryKey<T> key) {
        var registry = registries.get(key);
        //noinspection unchecked
        return (DataRegistry<T>) Objects.requireNonNull(registry);
    }
}
