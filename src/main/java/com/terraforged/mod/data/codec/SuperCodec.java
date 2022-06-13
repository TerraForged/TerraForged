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

package com.terraforged.mod.data.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.terraforged.cereal.Cereal;
import com.terraforged.cereal.spec.Context;
import com.terraforged.cereal.spec.DataSpecs;
import com.terraforged.mod.data.util.DataUtil;

import java.util.function.Supplier;

public record SuperCodec<V>(Class<V> type, Supplier<Void> validator) implements Codec<V> {
    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        try {
            validator.get();
            var json = ops.convertTo(JsonOps.INSTANCE, input);
            var data = DataUtil.toData(json);
            V result = Cereal.deserialize(data.asObj(), type, Context.NONE);
            return DataResult.success(Pair.of(result, input));
        } catch (Throwable t) {
            t.printStackTrace();
            return DataResult.error(t.getMessage());
        }
    }

    @Override
    public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        try {
            validator.get();
            var data = Cereal.serialize(input, Context.NONE);
            var json = DataUtil.toJson(data);
            var output = JsonOps.INSTANCE.convertTo(ops, json);
            return DataResult.success(output);
        } catch (Throwable t) {
            t.printStackTrace();
            return DataResult.error(t.getMessage());
        }
    }

    private static Supplier<Void> getValidator(Class<?> type) {
        return Suppliers.memoize(() -> {
            if (DataSpecs.getSubSpec(type) == null) {
                throw new IllegalStateException("No sub-spec for type: " + type);
            }
            return null;
        });
    }

    private static final Supplier<Void> NOOP_VALIDATOR = () -> null;

    public static <T> SuperCodec<T> of(Class<T> type) {
        return new SuperCodec<>(type, getValidator(type));
    }

    public static <T> SuperCodec<T> withoutValidator(Class<T> type) {
        return new SuperCodec<>(type, NOOP_VALIDATOR);
    }
}
