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

import java.util.ArrayList;
import java.util.List;

public class Geology<T> {

    private final Module selector;
    private final List<Strata<T>> backing = new ArrayList<>();

    public Geology(Module selector) {
        this.selector = selector;
    }

    public Geology<T> add(Geology<T> geology) {
        backing.addAll(geology.backing);
        return this;
    }

    public Geology<T> add(Strata<T> strata) {
        backing.add(strata);
        return this;
    }

    public Strata<T> getStrata(float x, int y) {
        float noise = selector.getValue(x, y);
        return getStrata(noise);
    }

    public Strata<T> getStrata(float value) {
        int index = (int) (value * backing.size());
        index = Math.min(backing.size() - 1, index);
        return backing.get(index);
    }
}
