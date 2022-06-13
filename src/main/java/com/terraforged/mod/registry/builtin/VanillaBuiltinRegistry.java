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

import com.terraforged.mod.registry.VanillaRegistry;
import com.terraforged.mod.registry.key.RegistryKey;
import com.terraforged.mod.registry.lazy.LazyValue;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.Map;
import java.util.stream.Stream;

public class VanillaBuiltinRegistry<T> extends LazyValue<Registry<T>> implements VanillaRegistry<T> {
    protected final RegistryKey<T> key;

    public VanillaBuiltinRegistry(RegistryKey<T> key) {
        super(null);
        this.key = key;
    }

    @Override
    public RegistryKey<T> key() {
        return key;
    }

    @Override
    public void register(ResourceKey<T> key, T value) {
        BuiltinRegistries.register(get(), key, value);
    }

    @Override
    public Stream<Map.Entry<ResourceKey<T>, T>> stream() {
        return get().entrySet().stream();
    }

    @Override
    protected Registry<T> compute() {
        var registry = BuiltinRegistries.REGISTRY.get(key.get().location());

        //noinspection unchecked
        return (Registry<T> ) registry;
    }
}
