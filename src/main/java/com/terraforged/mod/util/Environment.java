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

package com.terraforged.mod.util;

import com.terraforged.engine.Engine;
import com.terraforged.mod.Log;

public class Environment {

    private static final int MIN_CORES = 4;
    private static final boolean dev = System.getProperty("dev") != null;
    private static final boolean verbose = System.getProperty("verbose") != null;
    private static final boolean vanillaBiomes = System.getProperty("vanillaBiomes") != null;

    public static boolean isDev() {
        return dev;
    }

    public static boolean isVerbose() {
        return dev || verbose;
    }

    public static boolean isVanillaBiomes() {
        return vanillaBiomes;
    }

    public static void log() {
        Log.info("Environment: dev={}, stable={}, vanilla={}", dev, Engine.ENFORCE_STABLE_OPTIONS, vanillaBiomes);
        perf();
    }

    private static void perf() {
        int processors = Runtime.getRuntime().availableProcessors();
        if (processors < MIN_CORES) {
            Log.warn("Running on unsupported cpu! TerraForged may not work correctly! Available Processors: {}", processors);
            return;
        }

        if (processors == MIN_CORES) {
            Log.info("Running on minimum supported core-count. Performance may not be optimal!");
        }
    }
}