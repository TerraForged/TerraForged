/*
 *
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

import com.terraforged.core.util.serialization.serializer.Writer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class NBTWriter implements Writer {

    private final Context root = new Context(null);

    private String name = "";
    private Context context = root;

    public NBTWriter() {

    }

    public Tag root() {
        return root.value;
    }

    public CompoundTag compound() {
        return (CompoundTag) root();
    }

    public ListTag list() {
        return (ListTag) root();
    }

    private NBTWriter begin(Tag value) {
        if (root.value == null) {
            root.value = value;
            context.value = value;
        } else {
            append(value);
            context = new Context(context);
            context.value = value;
        }
        return this;
    }

    private NBTWriter append(Tag value) {
        if (context.value instanceof CompoundTag) {
            ((CompoundTag) context.value).put(name, value);
        } else if (context.value instanceof ListTag) {
            ((ListTag) context.value).add(value);
        }
        return this;
    }

    @Override
    public NBTWriter name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NBTWriter beginObject() {
        return begin(new CompoundTag());
    }

    @Override
    public NBTWriter endObject() {
        context = context.parent;
        return this;
    }

    @Override
    public NBTWriter beginArray() {
        return begin(new ListTag());
    }

    @Override
    public NBTWriter endArray() {
        context = context.parent;
        return this;
    }

    @Override
    public NBTWriter value(String value) {
        return append(StringTag.of(value));
    }

    @Override
    public NBTWriter value(float value) {
        return append(FloatTag.of(value));
    }

    @Override
    public NBTWriter value(int value) {
        return append(IntTag.of(value));
    }

    private static class Context {

        private final Context parent;
        private Tag value;

        private Context(Context root) {
            this.parent = root;
        }
    }
}
