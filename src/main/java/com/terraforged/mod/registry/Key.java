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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Supplier;

public abstract class Key<T> implements Supplier<T> {
    protected final ResourceLocation name;
    private volatile T value;

    public Key(ResourceLocation name) {
        this.name = name;
    }

    @Override
    public T get() {
        var value = this.value;
        if (value == null) {
            synchronized (name) {
                value = this.value;
                if (value == null) {
                    value = compute();
                    this.value = value;
                }
            }
        }
        return value;
    }

    protected abstract T compute();

    public static class LazyRegistry<T> extends Key<ResourceKey<Registry<T>>> {
        public LazyRegistry(ResourceLocation name) {
            super(name);
        }

        public ResourceKey<T> element(String name) {
            return ResourceKey.create(get(), TerraForged.location(name));
        }

        public Holder<T> holder(RegistryAccess access, String name) {
            if (access == null) return ModRegistries.holder(get(), name);

            var key = element(name);
            var registry = access.ownedRegistryOrThrow(get());
            return registry.getHolderOrThrow(key);
        }

        @Override
        protected ResourceKey<Registry<T>> compute() {
            return ResourceKey.createRegistryKey(name);
        }
    }

    public static class LazyTag<T> extends Key<TagKey<T>> {
        private final Supplier<ResourceKey<? extends Registry<T>>> registry;

        public LazyTag(Supplier<ResourceKey<? extends Registry<T>>> registry, ResourceLocation name) {
            super(name);
            this.registry = registry;
        }

        @Override
        protected TagKey<T> compute() {
            return TagKey.create(registry.get(), name);
        }
    }

    public static Key.LazyTag<Biome> biomeTag(String name) {
        return biomeTag(new ResourceLocation(name));
    }

    public static Key.LazyTag<Biome> biomeTag(ResourceLocation location) {
        return new Key.LazyTag<>(() -> Registry.BIOME_REGISTRY, location);
    }
}
