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
import com.terraforged.mod.registry.builtin.ModBuiltinRegistry;
import com.terraforged.mod.registry.builtin.VanillaBuiltinRegistry;
import com.terraforged.mod.registry.key.RegistryKey;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class RegistryManager {
    public static final RegistryManager DEFAULT = new RegistryManager(VanillaBuiltinRegistry::new, ModBuiltinRegistry::new);

    private final RegistryFactory.Mod modFactory;
    private final RegistryFactory.Vanilla vanillaFactory;
    private final RegistryHolder<ModRegistry<?>> modRegistries = new RegistryHolder<>();
    private final RegistryHolder<VanillaRegistry<?>> vanillaRegistries = new RegistryHolder<>();

    public RegistryManager(RegistryFactory.Vanilla vanillaFactory, RegistryFactory.Mod modFactory) {
        this.modFactory = modFactory;
        this.vanillaFactory = vanillaFactory;
    }

    public <T> void create(RegistryKey<T> registry) {
        vanillaRegistries.put(registry, vanillaFactory.create(registry));
    }

    public <T> void create(RegistryKey<T> registry, Codec<T> codec) {
        modRegistries.put(registry, modFactory.create(registry, codec));
    }

    public <T> ModRegistry<T> getModRegistry(RegistryKey<T> registry) {
        return modRegistries.getRegistry(registry);
    }

    public <T> VanillaRegistry<T> getVanillaRegistry(RegistryKey<T> registry) {
        return vanillaRegistries.getRegistry(registry);
    }

    public Iterable<ModRegistry<?>> getModRegistries() {
        return modRegistries.registries.values();
    }

    public Iterable<VanillaRegistry<?>> getVanillaRegistries() {
        return vanillaRegistries.registries.values();
    }

    protected static class RegistryHolder<Registry extends VanillaRegistry<?>> {
        private final Map<RegistryKey<?>, Registry> registries = new IdentityHashMap<>();

        public <T> void put(RegistryKey<T> key, Registry registry) {
            registries.put(key, registry);
        }

        public <T, R extends VanillaRegistry<T>> R getRegistry(RegistryKey<T> registry) {
            //noinspection unchecked
            return (R) Objects.requireNonNull(registries.get(registry));
        }
    }
}
