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

package com.terraforged.mod.registry.hooks;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.stream.Stream;

public record NetworkRegistryAccess(RegistryAccess registryAccess) implements RegistryAccess {
    @Override
    public <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> key) {
        if (!REGISTRIES.containsKey(key)) return Optional.empty();

        return registryAccess.ownedRegistry(key);
    }

    @Override
    public Stream<RegistryEntry<?>> ownedRegistries() {
        return registryAccess.ownedRegistries().filter(e -> REGISTRIES.containsKey(e.key()));
    }

    @Override
    public Stream<RegistryEntry<?>> networkSafeRegistries() {
        return RegistryAccess.super.networkSafeRegistries().filter(e -> REGISTRIES.containsKey(e.key()));
    }
}
