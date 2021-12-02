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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.terraforged.cereal.spec.DataFactory;
import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.spec.DataSpecs;
import com.terraforged.mod.util.DataUtil;

import java.util.function.Consumer;

public record SpecCodec<V>(DataSpec<V> spec) implements Codec<V> {
    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        try {
            var json = ops.convertTo(JsonOps.INSTANCE, input);
            var data = DataUtil.toData(json);
            V result = spec.deserialize(data.asObj());
            return DataResult.success(Pair.of(result, input));
        } catch (Throwable t) {
            t.printStackTrace();
            return DataResult.error(t.getMessage());
        }
    }

    @Override
    public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        try {
            var data = spec.serialize(input);
            var json = DataUtil.toJson(data);
            var output = JsonOps.INSTANCE.convertTo(ops, json);
            return DataResult.success(output);
        } catch (Throwable t) {
            t.printStackTrace();
            return DataResult.error(t.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Codec<T> of(String name) {
        return (Codec<T>) of(DataSpecs.getSpec(name));
    }

    public static <T> Codec<T> of(DataSpec<T> spec) {
        return new SpecCodec<>(spec);
    }

    public static <T> Codec<T> create(Class<T> type, DataFactory<T> factory, Consumer<DataSpec.Builder<T>> consumer) {
        var builder = DataSpec.builder(type, factory);
        consumer.accept(builder);
        return of(builder.build());
    }

    public static <T> Codec<T> create(String name, Class<T> type, DataFactory<T> factory, Consumer<DataSpec.Builder<T>> consumer) {
        var builder = DataSpec.builder(name, type, factory);
        consumer.accept(builder);
        return of(builder.build());
    }
}
