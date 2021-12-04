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

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.FileUtil;
import net.minecraft.world.level.DataPackConfig;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataPackExporter {
    private static final String PACK = "file/" + TerraForged.TITLE + ".zip";

    public static DataPackConfig setup(@Nullable Path dir, DataPackConfig config) {
        if (dir == null) throw new NullPointerException("Dir is null!");

        TerraForged.LOG.info("Generating TerraForged datapack");

        try {
            var dest = dir.resolve(TerraForged.TITLE + ".zip");
            var root = TerraForged.getPlatform().getContainer();
            FileUtil.createZipCopy(root, "default", dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> enabled = new ArrayList<>(config.getEnabled());
        enabled.add(PACK);

        List<String> disabled = new ArrayList<>(config.getDisabled());
        disabled.remove(PACK);

        return new DataPackConfig(enabled, disabled);
    }
}
