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

package com.terraforged.mod.worldgen;

import com.terraforged.mod.util.Timer;

import java.util.concurrent.TimeUnit;

public enum Stage {
    STRUCTURE_STARTS,
    STRUCTURE_REFS,
    BIOMES,
    FILL,
    SURFACE,
    CARVER,
    DECORATION,
    ;

    private static final Stage[] STAGES = values();

    private final Timer timer;

    Stage() {
        timer = new Timer(name(), 5, TimeUnit.SECONDS);
    }

    public Timer timer() {
        return timer;
    }

    public Timer.Instance start() {
        return timer.start();
    }

    public static void reset() {
        for (var stage : STAGES) {
            stage.timer.reset();
        }
    }
}
