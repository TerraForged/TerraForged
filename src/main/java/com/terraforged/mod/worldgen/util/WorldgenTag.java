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

package com.terraforged.mod.worldgen.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class WorldgenTag<T> {
    private final ObjectSet<T> items;

    public WorldgenTag(ObjectSet<T> items) {
        this.items = ObjectSets.unmodifiable(items);
    }

    public final ObjectSet<T> items() {
        return items;
    }

    public final boolean contains(T item) {
        return items.contains(item);
    }

    @Override
    public String toString() {
        return "WorldgenTag{" +
                "items=" + items +
                '}';
    }

    private static <T> ObjectSet<T> unwrap(List<Supplier<T>> list) {
        var set = new ObjectOpenHashSet<T>(list.size());
        for (var item : list) {
            set.add(item.get());
        }
        return set;
    }

    private static <T> List<Supplier<T>> wrap(Set<T> set) {
        var list = new ArrayList<Supplier<T>>(set.size());
        for (var item : set) {
            list.add(() -> item);
        }
        return list;
    }

    public static <V, T extends WorldgenTag<V>> Codec<T> codec(Codec<List<Supplier<V>>> listCodec, Function<ObjectSet<V>, T> constructor) {
        return codec("items", listCodec, constructor);
    }

    public static <V, T extends WorldgenTag<V>> Codec<T> codec(String key, Codec<List<Supplier<V>>> listCodec, Function<ObjectSet<V>, T> constructor) {
        return RecordCodecBuilder.create(instance -> instance.group(
                listCodec.fieldOf(key).xmap(WorldgenTag::unwrap, WorldgenTag::wrap).forGetter(WorldgenTag::items)
        ).apply(instance, constructor));
    }
}
