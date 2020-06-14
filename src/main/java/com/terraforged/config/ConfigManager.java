package com.terraforged.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.terraforged.Log;
import joptsimple.internal.Strings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class ConfigManager {

    private static final Path COMMON_DIR = Paths.get("config", "terraforged");

    public static final ConfigRef BIOME_WEIGHTS = new ConfigRef(() -> create("biome_weights", cfg -> {
        set(
                cfg,
                "terraforged:example_biome",
                10,
                "Configure biome weights by entering their id and an integer value for their weight (default weight is 10)",
                "This config will override the weights configured or provided by other mods for TerraForged worlds only."
        );
    }));

    public static final ConfigRef PERFORMANCE = new ConfigRef(() -> create("performance", cfg -> {
        set (
                cfg,
                "thread_count",
                PerfDefaults.THREAD_COUNT,
                "Controls the total number of threads that will be used to generate heightmap tiles.",
                "Allowing the generator to use more threads can help speed up generation but increases overall",
                "load on your CPU which may adversely affect performance in other areas of the game engine."
        );

        set(
                cfg,
                "tile_size",
                PerfDefaults.TILE_SIZE,
                "Controls the size of heightmap tiles.",
                "Smaller tiles are faster to generate but less memory efficient."
        );

        set(
                cfg,
                "batching",
                true,
                "Batching breaks heightmap tiles up into smaller pieces that can be generated concurrently.",
                "This can help improve generation speed by utilizing more threads.",
                "It is more effective when a higher thread count (+6) is available."
        );

        set(
                cfg,
                "batch_count",
                PerfDefaults.BATCH_SIZE,
                "Controls the number of pieces a heightmap tile is divided up into.",
                "Higher batch counts may be able to utilize more of the available threads, improving performance."
        );
    }));

    public static void init() {
        Config.setInsertionOrderPreserved(true);
        BIOME_WEIGHTS.get();
        PERFORMANCE.get();
        PerfDefaults.getAndPrintPerfSettings();
    }

    private static CommentedFileConfig create(String name, Consumer<CommentedFileConfig> defaulter) {
        Path path = COMMON_DIR.resolve(name + ".conf");
        if (!Files.exists(path)) {
            Log.info("Creating default config: {}", name);
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }
            CommentedFileConfig config = CommentedFileConfig.of(path, TomlFormat.instance());
            defaulter.accept(config);
            config.save();
            return config;
        } else {
            return CommentedFileConfig.of(path, TomlFormat.instance());
        }
    }

    private static <T> void set(CommentedConfig config, String path, T value, String... lines) {
        config.setComment(path, Strings.join(lines, "\n"));
        config.set(path, value);
    }
}
