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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.featuremanager.util.codec.Codecs;

import java.util.function.Function;

public class Component<Parent, Type> {

    private final String name;
    private final Codec<Type> codec;
    private final Function<Parent, Type> getter;

    protected Component(String name, Codec<Type> codec, Function<Parent, Type> getter) {
        this.name = name;
        this.codec = codec;
        this.getter = getter;
    }

    public String getName() {
        return name;
    }

    public <T> T encode(Parent parent, DynamicOps<T> ops) {
        Type type = getter.apply(parent);
        return Codecs.encodeAndGet(codec, type, ops);
    }
}
