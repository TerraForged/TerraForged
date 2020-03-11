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

import com.terraforged.core.util.serialization.serializer.Reader;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;

import java.util.Collection;

public class NBTReader implements Reader {

    private final Tag root;

    public NBTReader(Tag root) {
        this.root = root;
    }

    @Override
    public int getSize() {
        if (root instanceof CompoundTag) {
            return ((CompoundTag) root).getSize();
        }
        if (root instanceof ListTag) {
            return ((ListTag) root).size();
        }
        return 1;
    }

    @Override
    public NBTReader getChild(String key) {
        return new NBTReader(((CompoundTag) root).get(key));
    }

    @Override
    public NBTReader getChild(int index) {
        return new NBTReader(((ListTag) root).get(index));
    }

    @Override
    public Collection<String> getKeys() {
        return ((CompoundTag) root).getKeys();
    }

    @Override
    public String getString(String key) {
        return ((CompoundTag) root).getString(key);
    }

    @Override
    public float getFloat(String key) {
        return ((CompoundTag) root).getFloat(key);
    }

    @Override
    public int getInt(String key) {
        return ((CompoundTag) root).getInt(key);
    }

    @Override
    public String getString(int index) {
        return ((ListTag) root).getString(index);
    }

    @Override
    public float getFloat(int index) {
        return ((ListTag) root).getFloat(index);
    }

    @Override
    public int getInt(int index) {
        return ((ListTag) root).getInt(index);
    }

    @Override
    public String getString() {
        return root.asString();
    }

    @Override
    public boolean getBool() {
        return ((ByteTag) root).getByte() == 1;
    }

    @Override
    public float getFloat() {
        return ((FloatTag) root).getFloat();
    }

    @Override
    public int getInt() {
        return ((IntTag) root).getInt();
    }
}
