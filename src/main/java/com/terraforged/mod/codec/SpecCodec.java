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
