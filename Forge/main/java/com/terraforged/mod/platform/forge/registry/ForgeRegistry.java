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

package com.terraforged.mod.platform.forge.registry;

import com.google.common.base.Suppliers;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.VanillaRegistry;
import com.terraforged.mod.registry.key.RegistryKey;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.DeferredRegister;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

public class ForgeRegistry<T> implements VanillaRegistry<T> {
    protected final RegistryKey<T> key;
    protected final DeferredRegister<T> register;

    public ForgeRegistry(RegistryKey<T> key) {
        this.key = key;
        this.register = DeferredRegister.create(key.get(), TerraForged.MODID);
    }

    @Override
    public RegistryKey<T> key() {
        return key;
    }

    @Override
    public void register(ResourceKey<T> key, T value) {
        register.register(key.location().getPath(), Suppliers.ofInstance(value));
    }

    @Override
    public Stream<Map.Entry<ResourceKey<T>, T>> stream() {
        return register.getEntries().stream().map(o -> new AbstractMap.SimpleEntry<>(o.getKey(), o.get()));
    }
}
