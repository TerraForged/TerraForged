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
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.settings.preset.Preset;
import com.terraforged.mod.profiler.watchdog.Watchdog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class ConfigManager {

    private static final String PERF_VERSION = "0.1";
    private static final String GENERAL_VERSION = "0.4";
    private static final Path COMMON_DIR = Paths.get("config", "terraforged").toAbsolutePath();

    public static final ConfigRef BIOME_WEIGHTS = new ConfigRef(() -> create("biome_weights", cfg -> set(
            cfg,
            "terraforged:example_biome",
            10,
            "Configure biome weights by entering their id and an integer value for their weight (default weight is 10)",
            "This config will override the weights configured or provided by other mods for TerraForged worlds only."
    )));

    public static final ConfigRef PERFORMANCE = new ConfigRef(PERF_VERSION, version -> create("performance_internal", version, cfg -> {
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
                PerfDefaults.BATCHING,
                "Batching breaks heightmap tiles up into smaller pieces that can be generated concurrently.",
                "This can help improve generation speed by utilizing more threads.",
                "It is more effective when a higher thread count (+6) is available."
        );

        set(
                cfg,
                "batch_count",
                PerfDefaults.BATCH_COUNT,
                "Controls the number of pieces a heightmap tile is divided up into.",
                "Higher batch counts may be able to utilize more of the available threads, improving performance."
        );
    }));

    public static final ConfigRef GENERAL = new ConfigRef(GENERAL_VERSION, version -> create("general", version, cfg -> {
        set(
                cfg,
                Preset.DEFAULT_KEY,
                "default",
                "Set the preset to use when creating a new world"
        );
        set(
                cfg,
                "tooltips",
                true,
                "Set whether tooltips should be displayed by default in the config gui."
        );
        set(
                cfg,
                "coords",
                false,
                "Set whether coordinates should be displayed by default in the config gui."
        );
        set(
                cfg,
                Watchdog.FEATURE_WARN_KEY,
                100,
                "The number of milliseconds a single feature/structure can generate for before a warning",
                "is printed to the logs. This may help track down mods that are causing world-gen to run slow.",
                "Set to -1 to disable."
        );
        set(
                cfg,
                Watchdog.CHUNK_TIMEOUT_KEY,
                60_000,
                "The number of milliseconds after which the server will be considered 'deadlocked' (when it",
                "gets stuck trying to generate a feature/structure). This is usually caused by third-party mods.",
                "Set to -1 to disable deadlock detection & reporting (the game may freeze indefinitely without it)."
        );
    }));

    public static void init() {
        Config.setInsertionOrderPreserved(true);
        BIOME_WEIGHTS.get();
        PERFORMANCE.get();
        GENERAL.get();
        PerfDefaults.getAndPrintPerfSettings();
    }

    private static CommentedFileConfig create(String name, Consumer<CommentedFileConfig> defaulter) {
        return create(name, "", defaulter);
    }

    private static CommentedFileConfig create(String name, String version, Consumer<CommentedFileConfig> defaulter) {
        Path path = COMMON_DIR.resolve(name + ".conf");

        if (!Files.exists(path)) {
            Log.info("Creating default config: {}", name);
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        CommentedFileConfig config = CommentedFileConfig.of(path, TomlFormat.instance());
        config.load();

        update(config, version);
        defaulter.accept(config);
        config.save();

        return config;
    }

    private static void update(CommentedFileConfig config, String version) {
        if (version.isEmpty()) {
            return;
        }

        String currentVersion = config.getOrElse("version", "unversioned");
        if (currentVersion != null && currentVersion.equals(version)) {
            return;
        }

        Log.info("Updating config: {} -> {}", currentVersion, version);
        config.clear();
        config.setComment("version", " The version of this config. Do not edit (it'll wipe your settings!)");
        config.set("version", version);
    }

    private static <T> void set(CommentedConfig config, String path, T value, String... lines) {
        config.setComment(path, " " + String.join( "\n ", lines));

        if (!config.contains(path)) {
            config.set(path, value);
        }
    }
}
