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

package com.terraforged.mod.util;

public abstract class RangeModifier {

    protected final float from;
    protected final float to;
    protected final float max;
    private final float range;

    public RangeModifier(float from, float max, boolean exclusive) {
        this.from = from;
        this.to = max;
        this.max = exclusive ? 0 : 1;
        this.range = Math.abs(max - from);
    }

    public float apply(float value) {
        if (from < to) {
            if (value <= from) {
                return 0F;
            }
            if (value >= to) {
                return max;
            }
            return (value - from) / range;
        } else if (from > to) {
            if (value <= to) {
                return max;
            }
            if (value >= from) {
                return 0F;
            }
            return 1 - ((value - to) / range);
        }
        return 0F;
    }
}
