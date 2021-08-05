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

package com.terraforged.mod.config;

import com.terraforged.engine.concurrent.thread.ThreadPools;
import com.terraforged.engine.settings.FilterSettings;
import com.terraforged.mod.Log;

public class PerfDefaults {

    public static final int TILE_SIZE = 3;
    public static final int BATCH_COUNT = 6;
    public static final int THREAD_COUNT = ThreadPools.defaultPoolSize();

    public static int getTileBorderSize(FilterSettings settings) {
        // Scale tile border size with droplet lifetime
        return Math.min(2, Math.max(1, settings.erosion.dropletLifetime / 16));
    }

    public static void print() {
        Log.info("System Defaults:");
        Log.info(" Thread Count:  {}", THREAD_COUNT);
        Log.info(" Tile Size:     {}", TILE_SIZE);
        Log.info(" Batch Size:    {}", BATCH_COUNT);
    }
}
