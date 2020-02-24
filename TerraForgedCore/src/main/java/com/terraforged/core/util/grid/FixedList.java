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

package com.terraforged.core.util.grid;

import me.dags.noise.util.NoiseUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FixedList<T> implements Iterable<T> {

    private final int maxIndex;
    private final T[] elements;

    FixedList(T[] elements) {
        this.maxIndex = elements.length - 1;
        this.elements = elements;
    }

    public T get(int index) {
        if (index < 0) {
            return elements[0];
        }
        if (index > maxIndex) {
            return elements[maxIndex];
        }
        return elements[index];
    }

    public T get(float value) {
        return get(indexOf(value));
    }

    public int size() {
        return elements.length;
    }

    public int indexOf(float value) {
        return NoiseUtil.round(value * maxIndex);
    }

    public Set<T> uniqueValues() {
        Set<T> set = new HashSet<>();
        Collections.addAll(set, elements);
        return set;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < elements.length;
            }

            @Override
            public T next() {
                return elements[index++];
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> FixedList<T> of(List<T> list) {
        return new FixedList<>((T[]) list.toArray());
    }
}
