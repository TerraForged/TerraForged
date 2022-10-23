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

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.settings.preset.Preset;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.profiler.watchdog.Watchdog;
import com.terraforged.mod.structure.StructureLocator;
import com.terraforged.mod.util.TagFixer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class ConfigManager {

    private static final String GENERAL_VERSION = "1.2";
    private static final Path COMMON_DIR = Paths.get("config", "terraforged").toAbsolutePath();

    public static final ConfigRef BIOME_WEIGHTS = new ConfigRef(() -> create("biome_weights", cfg -> set(
            cfg,
            "#terraforged:example_biome",
            10,
            "Configure biome weights by entering their id and an integer value for their weight (default weight is 10)",
            "This config will override the weights configured or provided by other mods for TerraForged worlds only."
    )));

    public static final ConfigRef GENERAL = new ConfigRef(GENERAL_VERSION, version -> create("general", version, cfg -> {
        set(
                cfg,
                Preset.DEFAULT_KEY,
                Preset.DEFAULT_PRESET,
                "Set the preset to use when creating a new world."
        );
        set(
                cfg,
                GuiKeys.TOOLTIPS_KEY,
                GuiKeys.DEFAULT_TOOLTIPS,
                "Set whether tooltips should be displayed by default in the config gui."
        );
        set(
                cfg,
                GuiKeys.COORDS_KEY,
                GuiKeys.DEFAULT_COORDS,
                "Set whether coordinates should be displayed by default in the config gui."
        );
        set(
                cfg,
                StructureLocator.ASYNC_KEY,
                StructureLocator.DEFAULT_ASYNC,
                "Set whether multiple threads should be used to perform structure searches.",
                "This can greatly increase the speed of finding structures via the /locate command."
        );
        set(
                cfg,
                StructureLocator.TIMEOUT_KEY,
                StructureLocator.DEFAULT_TIMEOUT_MS,
                "Set the number of milliseconds that a structure search can run for before it is aborted."
        );
        set(
                cfg,
                Watchdog.FEATURE_WARN_KEY,
                Watchdog.DEFAULT_WARN_TIME_MS,
                "The number of milliseconds a single feature/structure can generate for before a warning",
                "is printed to the logs. This may help track down mods that are causing world-gen to run slow.",
                "Set to -1 to disable."
        );
        set(
                cfg,
                Watchdog.CHUNK_TIMEOUT_KEY,
                Watchdog.DEFAULT_TIMEOUT_MS,
                "The number of milliseconds after which the server will be considered 'deadlocked' (when it",
                "gets stuck trying to generate a feature/structure). This is usually caused by third-party mods.",
                "Set to -1 to disable deadlock detection & reporting (the game may freeze indefinitely without it)."
        );
        set(
                cfg,
                TagFixer.FIX_BLOCK_TAG_KEY,
                TagFixer.FIX_BLOCK_TAG_DEFAULT,
                "Fixes a vanilla serialization bug for block tags which TerraForged relies on",
                "to slightly increase the vanilla ore distribution to suit the taller terrain.",
                "Disable if this causes issues with other mods."
        );
    }));

    public static void init() {
        Config.setInsertionOrderPreserved(true);
        BIOME_WEIGHTS.get();
        GENERAL.get();
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
        // # denotes an example/non-required entry - only add it if no other values exist
        if (path.startsWith("#")) {
            if (config.isEmpty()) {
                path = path.substring(1);
                config.setComment(path, " " + String.join( "\n ", lines));
                config.set(path, value);
            }
        } else if (!config.contains(path)) {
            config.setComment(path, " " + String.join( "\n ", lines));
            config.set(path, value);
        }
    }
}
