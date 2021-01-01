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

package com.terraforged.mod.util;

import com.mojang.serialization.Lifecycle;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.ThreadLocalResource;
import com.terraforged.mod.LevelType;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.settings.DimensionSettings;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public class DimUtils {

    private static final ThreadLocalResource<Map<ForgeWorldType, SimpleRegistry<Dimension>>> CACHE = ThreadLocalResource.withInitial(HashMap::new, Map::clear);

    public static DimensionGeneratorSettings populateDimensions(DimensionGeneratorSettings level, DynamicRegistries registries, DimensionSettings settings) {
        try (Resource<?> ignored = CACHE.get()) {
            // replace nether with custom dimension if present
            overrideDimension(Dimension.THE_NETHER, settings.dimensions.nether, level, registries);
            // replace end with custom dimension if present
            overrideDimension(Dimension.THE_END, settings.dimensions.end, level, registries);
            // scan forge registry for other level-types and add their extra (ie not overworld,nether,end) dimensions
            if (settings.dimensions.includeExtraDimensions) {
                addExtraDimensions(level, registries);
            }
        }
        return level;
    }

    private static void overrideDimension(RegistryKey<Dimension> key, String levelType, DimensionGeneratorSettings level, DynamicRegistries registries) {
        SimpleRegistry<Dimension> dimensions = getDimensions(levelType, level, registries);
        if (dimensions == null) {
            return;
        }

        Dimension dimension = dimensions.getValueForKey(key);
        if (dimension == null) {
            return;
        }

        // replace the current Dimension assigned to the key without changing the id
        Log.info("Overriding dimension {} with {}'s", key.getLocation(), levelType);
        level.func_236224_e_().validateAndRegister(OptionalInt.empty(), key, dimension, Lifecycle.stable());
    }

    private static void addExtraDimensions(DimensionGeneratorSettings level, DynamicRegistries registries) {
        for (ForgeWorldType type : ForgeRegistries.WORLD_TYPES) {
            if (type == LevelType.TERRAFORGED) {
                continue;
            }

            SimpleRegistry<Dimension> dimensions = getDimensions(type, level, registries);
            if (dimensions == null) {
                continue;
            }

            for (Map.Entry<RegistryKey<Dimension>, Dimension> entry : dimensions.getEntries()) {
                // skip existing dims
                if (level.func_236224_e_().getValueForKey(entry.getKey()) != null) {
                    continue;
                }
                Log.info("Adding extra dimension {} dimension from {}", entry.getKey().getLocation(), type.getRegistryName());
                level.func_236224_e_().register(entry.getKey(), entry.getValue(), Lifecycle.stable());
            }
        }
    }

    @Nullable
    private static SimpleRegistry<Dimension> getDimensions(String levelType, DimensionGeneratorSettings level, DynamicRegistries registries) {
        ForgeWorldType type = getLevelType(levelType);
        return getDimensions(type, level, registries);
    }

    @Nullable
    private static SimpleRegistry<Dimension> getDimensions(ForgeWorldType type, DimensionGeneratorSettings level, DynamicRegistries registries) {
        // ignore our own level-type
        if (type == null || type == LevelType.TERRAFORGED) {
            return null;
        }

        return CACHE.open().computeIfAbsent(type, t -> {
            long seed = level.getSeed();
            boolean chest = level.hasBonusChest();
            boolean structures = level.doesGenerateFeatures();
            return t.createSettings(registries, seed, structures, chest, "").func_236224_e_();
        });
    }

    public static String getDisplayString(ForgeWorldType type) {
        if (type == LevelType.TERRAFORGED) {
            return "default";
        }
        return "" + type.getRegistryName();
    }

    public static ForgeWorldType getLevelType(String name) {
        if (name.equalsIgnoreCase("default")) {
            return LevelType.TERRAFORGED;
        }

        ResourceLocation location = ResourceLocation.tryCreate(name);
        if (location == null || location.equals(LevelType.TERRAFORGED.getRegistryName())) {
            return LevelType.TERRAFORGED;
        }

        ForgeWorldType type = ForgeRegistries.WORLD_TYPES.getValue(location);
        if (type == null) {
            return LevelType.TERRAFORGED;
        }

        return type;
    }
}
