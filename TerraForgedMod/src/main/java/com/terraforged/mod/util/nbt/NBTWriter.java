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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

public class NBTWriter implements Writer {

    private final Context root = new Context(null);

    private String name = "";
    private Context context = root;

    public NBTWriter() {

    }

    public INBT root() {
        return root.value;
    }

    public CompoundNBT compound() {
        return (CompoundNBT) root();
    }

    public ListNBT list() {
        return (ListNBT) root();
    }

    private NBTWriter begin(INBT value) {
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

    private NBTWriter append(INBT value) {
        if (context.value instanceof CompoundNBT) {
            ((CompoundNBT) context.value).put(name, value);
        } else if (context.value instanceof ListNBT) {
            ((ListNBT) context.value).add(value);
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
        return begin(new CompoundNBT());
    }

    @Override
    public NBTWriter endObject() {
        context = context.parent;
        return this;
    }

    @Override
    public NBTWriter beginArray() {
        return begin(new ListNBT());
    }

    @Override
    public NBTWriter endArray() {
        context = context.parent;
        return this;
    }

    @Override
    public NBTWriter value(String value) {
        return append(StringNBT.valueOf(value));
    }

    @Override
    public NBTWriter value(float value) {
        return append(FloatNBT.valueOf(value));
    }

    @Override
    public NBTWriter value(int value) {
        return append(IntNBT.valueOf(value));
    }

    private static class Context {

        private final Context parent;
        private INBT value;

        private Context(Context root) {
            this.parent = root;
        }
    }
}
