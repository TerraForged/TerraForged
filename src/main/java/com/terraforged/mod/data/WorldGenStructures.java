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

package com.terraforged.mod.data;

import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.DimensionSettings;

import java.io.File;

public class WorldGenStructures extends DataGen {

    private static final String PATH = "minecraft/"
            + Registry.NOISE_SETTINGS_KEY.getLocation().getPath() + "/"             // noise_settings
            + DimensionSettings.field_242734_c.getLocation().getPath() + ".json";   // overworld.json

    public static void genDefaultDimSettings(File dataDir) {
        WorldGenRegistries.NOISE_SETTINGS.getOptionalValue(DimensionSettings.field_242734_c)
                .ifPresent(settings -> genDimSettings(dataDir, settings));
    }

    public static void genDimSettings(File dataDir, DimensionSettings settings) {
        Codecs.encodeOpt(DimensionSettings.field_236097_a_, settings)
                .ifPresent(json -> write(json, dataDir, PATH));
    }
}
