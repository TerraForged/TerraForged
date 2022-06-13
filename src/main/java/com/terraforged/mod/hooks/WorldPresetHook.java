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

package com.terraforged.mod.hooks;

import com.terraforged.mod.util.ReflectionUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

public class WorldPresetHook {
    private static final MethodHandle REGISTRY_GETTER = ReflectionUtil.field(WorldPreset.class, Map.class);

    public static Map<ResourceKey<LevelStem>, LevelStem> getDimensions(WorldPreset preset) {
        return new HashMap<>(getDimensionsInternal(preset));
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceKey<LevelStem>, LevelStem> getDimensionsInternal(WorldPreset preset) {
        try {
            return (Map<ResourceKey<LevelStem>, LevelStem>) REGISTRY_GETTER.invokeExact((WorldPreset) preset);
        } catch (Throwable e) {
            e.printStackTrace();
            return Map.of();
        }
    }
}
