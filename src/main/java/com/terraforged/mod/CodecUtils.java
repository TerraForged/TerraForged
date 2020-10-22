package com.terraforged.mod;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.NoSuchElementException;
import java.util.Optional;

public class CodecUtils {

    public static <V, T> Optional<V> decode(Codec<Either<V, V>> codec, T t, DynamicOps<T> ops) {
        return codec.decode(ops, t).map(Pair::getFirst).flatMap(CodecUtils::either).result();
    }

    public static <T> DataResult<T> either(Either<T, T> either) {
        if (either.left().isPresent()) {
            return DataResult.success(either.left().get());
        }
        return either.right().map(DataResult::success).orElseGet(() -> DataResult.error("No result?! D:"));
    }

    public static <L, R> Codec<L> toLeft(Codec<Either<L, R>> codec) {
        return codec.xmap(e -> e.left().orElseThrow(NoSuchElementException::new), Either::left);
    }

    public static <L, R> Codec<R> toRight(Codec<Either<L, R>> codec) {
        return codec.xmap(e -> e.right().orElseThrow(NoSuchElementException::new), Either::right);
    }

    public static <T> Codec<T> toEither(Codec<Either<T, T>> codec) {
        return codec.xmap(e -> e.left().orElseGet(() -> e.right().orElseThrow(NoSuchElementException::new)), Either::left);
    }
}
