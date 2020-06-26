package com.terraforged.mod.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.terraforged.mod.Log;
import com.terraforged.core.concurrent.thread.ThreadPools;

public class PerfDefaults {

    public static final boolean BATCHING = false;
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
