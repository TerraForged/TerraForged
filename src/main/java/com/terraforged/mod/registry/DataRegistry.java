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
import com.mojang.serialization.Lifecycle;
import com.terraforged.mod.hooks.RegistryAccessUtil;
import com.terraforged.mod.registry.key.RegistryKey;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DataRegistry<T> implements Iterable<Map.Entry<ResourceKey<T>, T>> {
    protected final MappedRegistry<T> registry;
    protected final RegistryKey<T> key;
    protected final Codec<T> codec;

    public DataRegistry(RegistryKey<T> key, Codec<T> codec) {
        this.key = key;
        this.codec = codec;
        this.registry = new MappedRegistry<>(key.get(), Lifecycle.stable(), null);
    }

    public Codec<T> codec() {
        return codec;
    }

    public RegistryKey<T> key() {
        return key;
    }

    public void register(ResourceKey<T> key, T value) {
        registry.register(key, value, Lifecycle.stable());
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<ResourceKey<T>, T>> iterator() {
        return registry.entrySet().iterator();
    }

    public Stream<Map.Entry<ResourceKey<T>, T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public MappedRegistry<T> copy() {
        return RegistryAccessUtil.copy(registry);
    }
}
