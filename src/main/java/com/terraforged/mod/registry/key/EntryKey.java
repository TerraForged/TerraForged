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

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.lazy.LazyValue;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class EntryKey<T> extends LazyValue<ResourceKey<T>> {
    protected final Supplier<ResourceKey<Registry<T>>> registry;

    protected EntryKey(Supplier<ResourceKey<Registry<T>>> registry, ResourceLocation name) {
        super(name);
        this.registry = registry;
    }

    @Override
    protected ResourceKey<T> compute() {
        return ResourceKey.create(registry.get(), name);
    }

    public static <T> EntryKey<T> of(ResourceKey<Registry<T>> key, String elementName) {
        var name = TerraForged.location(elementName);
        return new EntryKey<>(() -> key, name);
    }
}
