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

import com.terraforged.mod.TerraForged;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class LazyKey<T> implements Supplier<ResourceKey<Registry<T>>> {
    private final ResourceLocation name;
    private volatile ResourceKey<Registry<T>> key;

    public LazyKey(ResourceLocation name) {
        this.name = name;
    }

    @Override
    public ResourceKey<Registry<T>> get() {
        var key = this.key;
        if (key == null) {
            synchronized (name) {
                key = this.key;
                if (this.key == null) {
                    key = ResourceKey.createRegistryKey(name);
                    this.key = key;
                }
            }
        }
        return key;
    }

    public ResourceKey<T> element(String name) {
        return ResourceKey.create(get(), TerraForged.location(name));
    }

    public Supplier<T> getter(RegistryAccess access, String name) {
        if (access == null) return ModRegistries.supplier(get(), name);

        var key = element(name);
        var registry = access.ownedRegistryOrThrow(get());
        return () -> registry.get(key);
    }
}
