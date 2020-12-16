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

package com.terraforged.mod.util.nbt;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.terraforged.engine.serialization.serializer.Deserializer;
import com.terraforged.engine.serialization.serializer.Reader;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicReader<T> implements Reader {

    private final T value;
    private final DynamicOps<T> ops;
    private final StreamIndexer<T> indexer = new StreamIndexer<>();

    public DynamicReader(T value, DynamicOps<T> ops) {
        this.value = value;
        this.ops = ops;
    }

    @Override
    public int getSize() {
        // attempt to count map. -1 == not a map
        int mapSize = ops.getMapValues(value).result().map(Stream::count).orElse(-1L).intValue();
        if (mapSize > -1) {
            return mapSize;
        }

        // attempt to count list. -1 == not a list
        int listSize = ops.getStream(value).result().map(Stream::count).orElse(-1L).intValue();
        if (listSize > -1) {
            return listSize;
        }

        return 0;
    }

    @Override
    public Reader getChild(String key) {
        T child = ops.get(value, key).result().orElseGet(ops::emptyMap);
        return new DynamicReader<>(child, ops);
    }

    @Override
    public Reader getChild(int index) {
        indexer.set(index);
        T child = ops.getStream(value).result().orElseGet(Stream::empty).filter(indexer).findFirst().orElseGet(ops::empty);
        return new DynamicReader<>(child, ops);
    }

    @Override
    public Collection<String> getKeys() {
        // this is horrible. thanks
        return ops.getMapValues(value).result().orElseGet(Stream::empty)
                .map(Pair::getFirst)
                .map(ops::getStringValue)
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public String getString() {
        return ops.getStringValue(value).result().orElse("");
    }

    @Override
    public boolean getBool() {
        return ops.getBooleanValue(value).result().orElse(false);
    }

    @Override
    public float getFloat() {
        return ops.getNumberValue(value).result().map(Number::floatValue).orElse(0F);
    }

    @Override
    public int getInt() {
        return ops.getNumberValue(value).result().map(Number::intValue).orElse(0);
    }

    private static class StreamIndexer<T> implements Predicate<T> {

        private int index;
        private int count;

        public void set(int index) {
            this.index = index;
            this.count = -1;
        }

        @Override
        public boolean test(T t) {
            return ++count == index;
        }
    }

    public static DynamicReader<JsonElement> json(JsonElement root) {
        return new DynamicReader<>(root, JsonOps.INSTANCE);
    }

    public static DynamicReader<INBT> nbt(INBT root) {
        return new DynamicReader<>(root, NBTDynamicOps.INSTANCE);
    }

    public static <T> DynamicReader<T> of(Dynamic<T> dynamic) {
        return of(dynamic.getValue(), dynamic.getOps());
    }

    public static <T> DynamicReader<T> of(T value, DynamicOps<T> ops) {
        return new DynamicReader<>(value, ops);
    }

    public static <V, T> V deserialize(V value, T data, DynamicOps<T> ops) {
        try {
            Deserializer.deserialize(DynamicReader.of(data, ops), value);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return value;
    }

    public static <V, T> V deserialize(V value, Dynamic<T> dynamic) {
        return deserialize(value, dynamic.getValue(), dynamic.getOps());
    }
}
