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

package com.terraforged.mod.registry.key;

import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.lazy.LazyHolder;
import com.terraforged.mod.registry.lazy.LazyValue;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegistryKey<T> extends LazyValue<ResourceKey<Registry<T>>> {
    public RegistryKey(ResourceLocation name) {
        super(name);
    }

    public EntryKey<T> entryKey(String name) {
        return new EntryKey<>(this, TerraForged.location(name));
    }

    public Holder<T> holder(String name, RegistryAccess access, Supplier<T> defaultSupplier) {
        var key = entryKey(name);
        if (access == null) {
            return new LazyHolder<>(defaultSupplier.get(), key);
        }
        return access.ownedRegistryOrThrow(get()).getHolderOrThrow(key.get());
    }

    public T[] entries(RegistryAccess access, IntFunction<T[]> arrayFunc) {
        if (access == null) {
            return toSortedArray(CommonAPI.get().getRegistryManager().getRegistry(this).stream(), arrayFunc);
        }
        return toSortedArray(access.ownedRegistryOrThrow(get()).entrySet().stream(), arrayFunc);
    }

    @Override
    protected ResourceKey<Registry<T>> compute() {
        return ResourceKey.createRegistryKey(name);
    }

    @Override
    public String toString() {
        return "RegistryKey{" + name + "}";
    }

    private static <T> T[] toSortedArray(Stream<Map.Entry<ResourceKey<T>, T>> stream, IntFunction<T[]> arrayFunc) {
        return stream.sorted(Comparator.comparing(e -> e.getKey().location()))
                .map(Map.Entry::getValue)
                .toArray(arrayFunc);
    }
}
