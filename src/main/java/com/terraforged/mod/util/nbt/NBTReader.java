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

package com.terraforged.mod.util.nbt;

import com.terraforged.engine.serialization.serializer.Reader;
import net.minecraft.nbt.*;

import java.util.Collection;

public class NBTReader implements Reader {

    private final INBT root;

    public NBTReader(INBT root) {
        this.root = root;
    }

    @Override
    public int getSize() {
        if (root instanceof CompoundNBT) {
            return ((CompoundNBT) root).size();
        }
        if (root instanceof ListNBT) {
            return ((ListNBT) root).size();
        }
        return 1;
    }

    @Override
    public NBTReader getChild(String key) {
        return new NBTReader(((CompoundNBT) root).get(key));
    }

    @Override
    public NBTReader getChild(int index) {
        return new NBTReader(((ListNBT) root).get(index));
    }

    @Override
    public Collection<String> getKeys() {
        return ((CompoundNBT) root).keySet();
    }

    @Override
    public String getString(String key) {
        return ((CompoundNBT) root).getString(key);
    }

    @Override
    public float getFloat(String key) {
        return ((CompoundNBT) root).getFloat(key);
    }

    @Override
    public int getInt(String key) {
        return ((CompoundNBT) root).getInt(key);
    }

    @Override
    public String getString(int index) {
        return ((ListNBT) root).getString(index);
    }

    @Override
    public float getFloat(int index) {
        return ((ListNBT) root).getFloat(index);
    }

    @Override
    public int getInt(int index) {
        return ((ListNBT) root).getInt(index);
    }

    @Override
    public String getString() {
        return root.getString();
    }

    @Override
    public boolean getBool() {
        return ((ByteNBT) root).getByte() == 1;
    }

    @Override
    public float getFloat() {
        return ((FloatNBT) root).getFloat();
    }

    @Override
    public int getInt() {
        return ((IntNBT) root).getInt();
    }
}
