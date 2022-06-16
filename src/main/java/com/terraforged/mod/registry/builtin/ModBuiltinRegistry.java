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

package com.terraforged.mod.registry.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.registry.key.RegistryKey;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;

import java.util.Map;
import java.util.stream.Stream;

public class ModBuiltinRegistry<T> implements ModRegistry<T> {
    protected final MappedRegistry<T> registry;
    protected final RegistryKey<T> key;
    protected final Codec<T> codec;

    public ModBuiltinRegistry(RegistryKey<T> key, Codec<T> codec) {
        this.key = key;
        this.codec = codec;
        this.registry = new MappedRegistry<>(key.get(), Lifecycle.stable(), null);
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }

    @Override
    public RegistryKey<T> key() {
        return key;
    }

    @Override
    public void register(ResourceKey<T> key, T value) {
        registry.register(key, value, Lifecycle.stable());
    }

    @Override
    public Stream<Map.Entry<ResourceKey<T>, T>> stream() {
        return registry.entrySet().stream();
    }

    @Override
    public MappedRegistry<T> copy() {
        var copy = new MappedRegistry<>(registry.key(), registry.lifecycle(), null);
        for (var value : registry) {
            int id = registry.getId(value);
            var key = registry.getResourceKey(value).orElseThrow();
            var lifecycle = registry.lifecycle(value);
            copy.registerMapping(id, key, value, lifecycle);
        }
        return copy;
    }
}
