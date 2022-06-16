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

package com.terraforged.mod.worldgen.datapack;

import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.FileUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataPackExporter {
    public static final String PACK_NAME = TerraForged.TITLE + "-" + TerraForged.DATAPACK_VERSION;
    public static final String PACK_FILE_NAME = PACK_NAME + ".zip";

    public static final Path CONFIG_DIR = Paths.get("config", "terraforged").toAbsolutePath();
    public static final Path DEFAULT_PACK_DIR = CONFIG_DIR.resolve("pack-" + TerraForged.DATAPACK_VERSION);

    public static void extractDefaultPack() {
        try {
            TerraForged.LOG.info("Extracting default datapack to {}", DEFAULT_PACK_DIR);

            var root = CommonAPI.get().getContainer();
            FileUtil.createDirCopy(root, "default", DEFAULT_PACK_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Pair<Path, String> getDefaultsPath() {
        if (!Files.exists(DEFAULT_PACK_DIR)) {
            extractDefaultPack();

            if (!Files.exists(DEFAULT_PACK_DIR)) {
                TerraForged.LOG.warn("Failed to extract default datapack to {}", DEFAULT_PACK_DIR);
                return Pair.of(CommonAPI.get().getContainer(), "default");
            }
        }
        return Pair.of(DEFAULT_PACK_DIR, ".");
    }

    public static void createWorldDatapack(Path dir) {
        TerraForged.LOG.info("Copying world-instance datapack to {}", dir);

        try {
            var src = getDefaultsPath();
            var dest = dir.resolve(PACK_FILE_NAME);
            FileUtil.createZipCopy(src.getLeft(), src.getRight(), dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
