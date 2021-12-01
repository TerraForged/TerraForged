package com.terraforged.mod.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.terraforged.cereal.Cereal;
import com.terraforged.cereal.spec.Context;
import com.terraforged.cereal.spec.DataSpecs;
import com.terraforged.mod.util.DataUtil;

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

    public static <T> SuperCodec<T> of(Class<T> type) {
        return new SuperCodec<>(type, getValidator(type));
    }
}
