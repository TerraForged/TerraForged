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

package com.terraforged.core.world.geology;

import me.dags.noise.Module;
import me.dags.noise.Source;

public class Stratum<T> {

    private final T value;
    private final Module depth;

    public Stratum(T value, double depth) {
        this(value, Source.constant(depth));
    }

    public Stratum(T value, Module depth) {
        this.depth = depth;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public float getDepth(float x, float z) {
        return depth.getValue(x, z);
    }

    public static <T> Stratum<T> of(T t, double depth) {
        return new Stratum<>(t, depth);
    }

    public static <T> Stratum<T> of(T t, Module depth) {
        return new Stratum<>(t, depth);
    }

    public interface Visitor<T> {

        boolean visit(int y, T value);
    }
}
