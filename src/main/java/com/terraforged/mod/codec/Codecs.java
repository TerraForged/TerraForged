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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;

import java.util.Optional;
import java.util.function.UnaryOperator;

public class Codecs {
    public static <A> MapCodec<A> opt(String name, A defaultValue, Codec<A> codec) {
        return Codec.optionalField(name, codec).xmap(o -> o.orElse(defaultValue), a -> Optional.ofNullable(a));
    }

    public static <V> JsonElement encode(V v, Codec<V> codec) {
        return codec.encodeStart(JsonOps.INSTANCE, v).result()
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .orElse(null);
    }

    public static <V> V modify(V v, Codec<V> codec, UnaryOperator<JsonObject> modifier) {
        var json = encode(v, codec);

        if (json == null) return v;

        var result = modifier.apply(json.getAsJsonObject());

        return codec.decode(JsonOps.INSTANCE, result).result().map(Pair::getFirst).orElse(v);
    }
}
