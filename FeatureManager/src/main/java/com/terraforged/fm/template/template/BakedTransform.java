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

package com.terraforged.fm.template.template;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import java.util.function.IntFunction;

public abstract class BakedTransform<T> {

    public static final int MIRRORS = Mirror.values().length;
    public static final int ROTATIONS = Rotation.values().length;

    private final T[] backing;

    public BakedTransform(IntFunction<T[]> array, T value) {
        backing = array.apply(MIRRORS * ROTATIONS);
        for (Mirror mirror : Mirror.values()) {
            for (Rotation rotation : Rotation.values()) {
                T result = apply(mirror, rotation, value);
                backing[indexOf(mirror, rotation)] = result;
            }
        }
    }

    public T get(Mirror mirror, Rotation rotation) {
        return backing[indexOf(mirror, rotation)];
    }

    protected abstract T apply(Mirror mirror, Rotation rotation, T value);

    private static int indexOf(Mirror mirror, Rotation rotation) {
        return mirror.ordinal() * ROTATIONS + rotation.ordinal();
    }
}
