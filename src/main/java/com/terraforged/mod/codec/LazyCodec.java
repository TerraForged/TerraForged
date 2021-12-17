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

package com.terraforged.mod.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;
import java.util.function.Supplier;

public interface LazyCodec<V> extends Codec<V> {
    @Override
    <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix);

    @Override
    <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input);

    record Instance<V>(Supplier<Codec<V>> supplier) implements LazyCodec<V> {
        @Override
        public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
            return supplier.get().encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
            return supplier.get().decode(ops, input);
        }
    }

    static <V> LazyCodec<V> of(Supplier<Codec<V>> supplier) {
        return new Instance<>(Suppliers.memoize(supplier::get));
    }

    static <V> LazyCodec<V> record(Function<RecordCodecBuilder.Instance<V>, ? extends App<RecordCodecBuilder.Mu<V>, V>> builder) {
        return new Instance<>(Suppliers.memoize(() -> RecordCodecBuilder.create(builder)));
    }

    static <V> LazyCodec<Supplier<V>> registry(Codec<V> codec, Supplier<ResourceKey<Registry<V>>> key) {
        return new Instance<>(() -> RegistryFileCodec.create(key.get(), codec));
    }
}
