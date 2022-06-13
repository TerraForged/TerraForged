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

package com.terraforged.mod.util.storage;

public class FloatMap {
    private final Index index;
    private final float[] data;

    public FloatMap() {
        index = Index.CHUNK;
        data = new float[16 * 16];
    }

    public FloatMap(int border) {
        int size = 16 + border * 2;
        this.index = Index.borderedChunk(border);
        this.data = new float[size * size];
    }

    public Index index() {
        return index;
    }

    public float get(int x, int z) {
        return get(index.of(x, z));
    }

    public void set(int x, int z, float value) {
        set(index.of(x, z), value);
    }

    public float get(int index) {
        return data[index];
    }

    public void set(int index, float value) {
        data[index] = value;
    }
}
