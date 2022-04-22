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

package com.terraforged.mod.registry.lazy;

import com.terraforged.mod.TerraForged;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Supplier;

public class LazyTag<T> extends LazyValue<TagKey<T>> {
    protected final Supplier<ResourceKey<? extends Registry<T>>> registry;

    public LazyTag(Supplier<ResourceKey<? extends Registry<T>>> registry, ResourceLocation name) {
        super(name);
        this.registry = registry;
    }

    @Override
    protected TagKey<T> compute() {
        return TagKey.create(registry.get(), name);
    }

    public static LazyTag<Biome> biome(String name) {
        return new LazyTag<>(() -> Registry.BIOME_REGISTRY, TerraForged.location(name));
    }

    public static <T> LazyTag<T> of(TagKey<T> tagKey) {
        var lazy = new LazyTag<>(tagKey::registry, tagKey.location());
        lazy.set(tagKey);
        return lazy;
    }
}
