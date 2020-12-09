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

package com.terraforged.mod.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.terraforged.core.concurrent.thread.ThreadPools;
import com.terraforged.mod.Log;

public class PerfDefaults {

    public static final boolean BATCHING = true;
    public static final int TILE_SIZE = 3;
    public static final int BATCH_COUNT = 6;
    public static final int THREAD_COUNT = ThreadPools.defaultPoolSize();

    public static final int MAX_TILE_SIZE = 8;
    public static final int MAX_BATCH_COUNT = 20;
    public static final int MAX_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private static boolean isUsingDefaultPerfSettings(CommentedConfig config) {
        boolean yes = true;
        yes &= config.getOrElse("batching", BATCHING);
        yes &= config.getInt("thread_count") == THREAD_COUNT;
        yes &= config.getInt("batch_count") == BATCH_COUNT;
        yes &= config.getInt("tile_size") == TILE_SIZE;
        return yes;
    }

    public static CommentedConfig getAndPrintPerfSettings() {
        CommentedConfig config = ConfigManager.PERFORMANCE.get();
        boolean defaults = isUsingDefaultPerfSettings(config);
        Log.info("Performance Settings [default={}]", defaults);
        Log.info(" - Thread Count: {}", config.getInt("thread_count"));
        Log.info(" - Tile Size: {}", config.getInt("tile_size"));
        Log.info(" - Batching: {}", config.getOrElse("batching", BATCHING));
        Log.info(" - Batch Count: {}", config.getInt("batch_count"));
        return config;
    }
}
