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

package com.terraforged.mod.featuremanager.util.codec.composite;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import com.terraforged.mod.featuremanager.util.codec.EncoderFunc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CompositeEncoder<V> implements EncoderFunc.Simple<V> {

    private final Codec<V> rootCodec;
    private final List<Component<V, ?>> components;

    private CompositeEncoder(Codec<V> rootCodec, List<Component<V, ?>> components) {
        this.rootCodec = rootCodec;
        this.components = components;
    }

    public <W> CompositeEncoder<V> with(String name, Function<V, W> getter, Codec<W> codec) {
        components.add(new Component<>(name, codec, getter));
        return this;
    }

    public CompositeEncoder<V> compile() {
        return new CompositeEncoder<>(rootCodec, ImmutableList.copyOf(components));
    }

    @Override
    public <T> T simpleEncode(V v, DynamicOps<T> ops) {
        Map<T, T> map = new HashMap<>();

        T root = Codecs.encodeAndGet(rootCodec, v, ops);
        ops.getMapEntries(root).result().ifPresent(result -> result.accept(map::put));

        for (Component<V, ?> component : components) {
            T key = ops.createString(component.getName());
            T value = component.encode(v, ops);
            map.put(key, value);
        }

        return ops.createMap(map);
    }

    public static <V> CompositeEncoder<V> of(Codec<V> rootCodec) {
        return new CompositeEncoder<>(rootCodec, new ArrayList<>());
    }
}
