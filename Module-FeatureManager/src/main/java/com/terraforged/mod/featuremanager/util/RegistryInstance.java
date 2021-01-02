/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.mod.featuremanager.util;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import org.jline.utils.Log;

import java.util.*;

public class RegistryInstance<T> implements Iterable<T>, Comparator<T> {

    private final Registry<T> registry;
    private final Map<T, T> remaps = new HashMap<>();

    public RegistryInstance(Registry<T> registry) {
        this.registry = registry;
        for (T t : registry) {
            Log.trace("- {}", t);
        }
    }

    public RegistryInstance(DynamicRegistries registries, RegistryKey<? extends Registry<T>> key) {
        this(registries.getRegistry(key));
    }

    public void addRemap(T in, T out) {
        remaps.put(in, out);
    }

    public T getRemap(T in) {
        return remaps.getOrDefault(in, in);
    }

    public Registry<T> getRegistry() {
        return registry;
    }

    public Optional<T> get(ResourceLocation name) {
        return registry.getOptional(name);
    }

    public T get(int id) {
        return registry.getByValue(id);
    }

    public T get(RegistryKey<T> key) {
        return registry.getValueForKey(key);
    }

    public T mustGet(ResourceLocation name) {
        return registry.getOrDefault(name);
    }

    public RegistryKey<T> getKey(T t) {
        return registry.getOptionalKey(t).orElse(null);
    }

    public ResourceLocation getRegistryName(T t) {
        return registry.getKey(t);
    }

    public int getId(T t) {
        return registry.getId(t);
    }

    public int getId(RegistryKey<T> key) {
        return registry.getId(get(key));
    }

    public String getName(T t) {
        return String.valueOf(registry.getKey(t));
    }

    public String getName(int id) {
        return String.valueOf(registry.getKey(get(id)));
    }

    public boolean contains(ResourceLocation name) {
       return registry.keySet().contains(name);
    }

    @Override
    public Iterator<T> iterator() {
        return registry.iterator();
    }

    @Override
    public int compare(T o1, T o2) {
        ResourceLocation k1 = registry.getKey(o1);
        ResourceLocation k2 = registry.getKey(o2);
        if (k1 == null || k2 == null) {
            return 0;
        }
        return k1.compareTo(k2);
    }
}
