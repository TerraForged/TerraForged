package com.terraforged.mod.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.util.RegistryUtil;
import net.minecraft.core.RegistryAccess;

public interface WorldGenCodec<V> extends Codec<V> {
    <T> V decode(DynamicOps<T> ops, T input, RegistryAccess access);

    <T> T encode(V v, DynamicOps<T> ops);

    @Override
    default <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        var access = RegistryUtil.getAccess(ops);
        if (access.isEmpty()) return DataResult.error("Invalid ops");

        var result = decode(ops, input, access.get());
        return DataResult.success(Pair.of(result, input));
    }

    @Override
    default <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        return DataResult.success(encode(input, ops));
    }
}
