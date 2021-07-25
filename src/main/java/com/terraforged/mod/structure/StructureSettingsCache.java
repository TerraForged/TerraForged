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

package com.terraforged.mod.structure;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Util to help prevent accidentally modifying the ChunkGenerator structure settings
 * when editing settings via the ui.
 */
public class StructureSettingsCache {

    private static final Map<DimensionStructuresSettings, DimensionStructuresSettings> CACHE = new ConcurrentHashMap<>();

    public static DimensionStructuresSettings get(DimensionStructuresSettings settings) {
        return CACHE.compute(settings, (key, value) -> {
            if (value == null) {
                return clone(key);
            }

            if (key.stronghold() != value.stronghold()) {
                return clone(key);
            }

            if (key.structureConfig() != value.structureConfig()) {
                return clone(key);
            }

            return value;
        });
    }

    public static void clear() {
        CACHE.clear();
    }

    private static DimensionStructuresSettings clone(DimensionStructuresSettings settings) {
        return new DimensionStructuresSettings(
                Optional.ofNullable(settings.stronghold()),
                ImmutableMap.copyOf(settings.structureConfig())
        );
    }
}
