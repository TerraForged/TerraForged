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

import com.terraforged.engine.serialization.serializer.AbstractWriter;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

public class NBTWriter extends AbstractWriter<INBT, CompoundNBT, ListNBT, NBTWriter> {

    public CompoundNBT compound() {
        return (CompoundNBT) get();
    }

    @Override
    protected NBTWriter self() {
        return this;
    }

    @Override
    protected boolean isObject(INBT value) {
        return value.getId() == Constants.NBT.TAG_COMPOUND;
    }

    @Override
    protected boolean isArray(INBT value) {
        return value.getId() == Constants.NBT.TAG_LIST;
    }

    @Override
    protected void add(CompoundNBT parent, String key, INBT value) {
        parent.put(key, value);
    }

    @Override
    protected void add(ListNBT parent, INBT value) {
        parent.add(value);
    }

    @Override
    protected CompoundNBT createObject() {
        return new CompoundNBT();
    }

    @Override
    protected ListNBT createArray() {
        return new ListNBT();
    }

    @Override
    protected INBT closeObject(CompoundNBT o) {
        return o;
    }

    @Override
    protected INBT closeArray(ListNBT a) {
        return a;
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

    @Override
    protected INBT create(boolean value) {
        return ByteNBT.valueOf(value);
    }
}
