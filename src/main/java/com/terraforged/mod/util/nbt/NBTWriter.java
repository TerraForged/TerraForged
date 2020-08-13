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

import com.terraforged.core.serialization.serializer.AbstractWriter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

public class NBTWriter extends AbstractWriter<INBT, NBTWriter> {

    public CompoundNBT compound() {
        return (CompoundNBT) getRoot();
    }

    @Override
    protected NBTWriter self() {
        return this;
    }

    @Override
    protected boolean isObject(INBT value) {
        return value instanceof CompoundNBT;
    }

    @Override
    protected boolean isArray(INBT value) {
        return value instanceof ListNBT;
    }

    @Override
    protected void add(INBT parent, String key, INBT value) {
        ((CompoundNBT) parent).put(key, value);
    }

    @Override
    protected void add(INBT parent, INBT value) {
        ((ListNBT) parent).add(value);
    }

    @Override
    protected INBT createObject() {
        return new CompoundNBT();
    }

    @Override
    protected INBT createArray() {
        return new ListNBT();
    }

    @Override
    protected INBT create(String value) {
        return StringNBT.valueOf(value);
    }

    @Override
    protected INBT create(int value) {
        return IntNBT.valueOf(value);
    }

    @Override
    protected INBT create(float value) {
        return FloatNBT.valueOf(value);
    }
}
