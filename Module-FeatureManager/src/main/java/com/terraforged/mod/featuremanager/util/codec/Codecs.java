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

package com.terraforged.mod.featuremanager.util.codec;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.terraforged.mod.featuremanager.FeatureManager;
import com.terraforged.mod.util.Environment;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class Codecs {

    public static final Marker MARKER = MarkerManager.getMarker("Codecs");
    private static final String[] FILTERS = {"tag"};

    public static <V> Codec<V> create(EncoderFunc<V> encoder, DecoderFunc<V> decoder) {
        return Codec.of(encoder, decoder).stable();
    }

    public static <V> Codec<V> create(EncoderFunc.Simple<V> encoder, DecoderFunc.Simple<V> decoder) {
        return Codec.of(encoder, decoder).stable();
    }

    public static <V, T> V decodeAndGet(Codec<V> codec, OptionalDynamic<T> dynamic) {
        return dynamic.result().flatMap(d -> decode(codec, d)).orElseThrow(CodecException.SUPPLIER);
    }

    public static <V, T> V decodeAndGet(Codec<V> codec, Dynamic<T> dynamic) {
        return decode(codec, dynamic).orElseThrow(CodecException.SUPPLIER);
    }

    public static <V, T> V decodeAndGet(Codec<V> codec, T value, DynamicOps<T> ops) {
        return decode(codec, value, ops).orElseThrow(CodecException.SUPPLIER);
    }

    public static <V, T> Optional<V> decode(Codec<V> codec, OptionalDynamic<T> dynamic) {
        return dynamic.result().flatMap(d -> decode(codec, d));
    }

    public static <V, T> Optional<V> decode(Codec<V> codec, Dynamic<T> dynamic) {
        return decode(codec, dynamic.getValue(), dynamic.getOps());
    }

    public static <V, T> Optional<V> decode(Codec<V> codec, T input, DynamicOps<T> ops) {
        Preconditions.checkNotNull(input);
        return getResult(codec.decode(ops, input)).map(Pair::getFirst);
    }

    public static <V> Optional<V> decode(Codec<V> codec, JsonElement element) {
        return decode(codec, element, JsonOps.INSTANCE);
    }

    public static <V, T> DataResult<T> encode(Codec<V> codec, V value, DynamicOps<T> ops) {
        Preconditions.checkNotNull(value);
        return codec.encodeStart(ops, value);
    }

    public static <V, T> T encodeAndGet(Codec<V> codec, V value, DynamicOps<T> ops) {
        Preconditions.checkNotNull(value);
        return getOrThrow(codec.encodeStart(ops, value));
    }

    public static <V> JsonElement encode(Codec<V> codec, V value) {
        Preconditions.checkNotNull(value);
        return getResult(encode(codec, value, JsonOps.INSTANCE)).orElse(JsonNull.INSTANCE);
    }

    public static <V> Optional<JsonElement> encodeOpt(Codec<V> codec, V value) {
        Preconditions.checkNotNull(value);
        return getResult(encode(codec, value, JsonOps.INSTANCE));
    }

    public static <V, T> T createList(Codec<V> codec, DynamicOps<T> ops, List<V> list) {
        return getResult(codec.listOf().encodeStart(ops, list)).orElseGet(() -> ops.createList(Stream.empty()));
    }

    public static <T> T getOrThrow(DataResult<T> result) {
        String error = null;
        if (result.error().isPresent()) {
            error = result.error().get().message();
        }
        return result.result().orElseThrow(CodecException.get(error));
    }

    public static <T> T modify(T value, Codec<T> codec, UnaryOperator<JsonElement> modifier) {
        try {
            JsonElement input = encode(codec, value);
            JsonElement output = modifier.apply(input);
            return decodeAndGet(codec, output, JsonOps.INSTANCE);
        } catch (Throwable t) {
            t.printStackTrace();
            return value;
        }
    }

    public static <V, T> Optional<V> getField(T value, Codec<T> codec, Function<JsonElement, V> mapper, String path) {
        return getField(value, codec, mapper, new String[]{path});
    }

    public static <V, T> Optional<V> getField(T value, Codec<T> codec, Function<JsonElement, V> mapper, String... path) {
        JsonElement element = encode(codec, value);

        for (String key : path) {
            if (!element.isJsonObject()) {
                return Optional.empty();
            }
            element = element.getAsJsonObject().get(key);
        }

        return Optional.ofNullable(mapper.apply(element));
    }

    public static <T> Optional<T> getResult(DataResult<T> result) {
        if (result.error().isPresent()) {
            String message = result.error().get().message();
            if (!Environment.isVerbose()) {
                String search = message.toLowerCase(Locale.ROOT);
                for (String filter : FILTERS) {
                    if (search.contains(filter)) {
                        return result.result();
                    }
                }
            }
            FeatureManager.LOG.log(Level.ERROR, MARKER, message);
        }
        return result.result();
    }
}
