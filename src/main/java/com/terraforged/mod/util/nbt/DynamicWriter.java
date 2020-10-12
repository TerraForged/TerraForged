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

package com.terraforged.mod.util.nbt;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.RecordBuilder;
import com.terraforged.core.serialization.serializer.AbstractWriter;
import com.terraforged.core.serialization.serializer.Serializer;
import com.terraforged.fm.util.codec.CodecException;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;

public class DynamicWriter<T> extends AbstractWriter<T, RecordBuilder<T>, ListBuilder<T>, DynamicWriter<T>> {

    private final DynamicOps<T> ops;

    public DynamicWriter(DynamicOps<T> ops) {
        this.ops = ops;
    }

    @Override
    protected DynamicWriter<T> self() {
        return this;
    }

    @Override
    protected boolean isObject(T value) {
        return ops.getMap(value).result().isPresent();
    }

    @Override
    protected boolean isArray(T value) {
        return ops.getList(value).result().isPresent();
    }

    @Override
    protected void add(RecordBuilder<T> parent, String key, T value) {
        parent.add(ops.createString(key), value);
    }

    @Override
    protected void add(ListBuilder<T> parent, T value) {
        parent.add(value);
    }

    @Override
    protected RecordBuilder<T> createObject() {
        return ops.mapBuilder();
    }

    @Override
    protected ListBuilder<T> createArray() {
        return ops.listBuilder();
    }

    @Override
    protected T closeObject(RecordBuilder<T> o) {
        return o.build(ops.empty()).result().orElseGet(ops::empty);
    }

    @Override
    protected T closeArray(ListBuilder<T> a) {
        return a.build(ops.empty()).result().orElseGet(ops::empty);
    }

    @Override
    protected T create(String value) {
        return ops.createString(value);
    }

    @Override
    protected T create(int value) {
        return ops.createInt(value);
    }

    @Override
    protected T create(float value) {
        return ops.createFloat(value);
    }

    public static DynamicWriter<JsonElement> json() {
        return new DynamicWriter<>(JsonOps.INSTANCE);
    }

    public static DynamicWriter<INBT> nbt() {
        return new DynamicWriter<>(NBTDynamicOps.INSTANCE);
    }

    public static <T> DynamicWriter<T> of(DynamicOps<T> ops) {
        return new DynamicWriter<>(ops);
    }

    public static <T> T serialize(Object value, DynamicOps<T> ops, boolean metaData) {
        DynamicWriter<T> writer = new DynamicWriter<>(ops);
        try {
            Serializer.serialize(value, writer, metaData);
        } catch (IllegalAccessException e) {
            throw CodecException.of(e, "Failed to serialize value %s", value);
        }
        return writer.get();
    }
}
