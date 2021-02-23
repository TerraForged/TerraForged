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

package com.terraforged.mod.util;

import com.mojang.serialization.Lifecycle;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.ThreadLocalResource;
import com.terraforged.mod.LevelType;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.settings.TerraSettings;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.EndBiomeProvider;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DimUtils {

    private static final ThreadLocalResource<Map<ForgeWorldType, SimpleRegistry<Dimension>>> CACHE = ThreadLocalResource.withInitial(HashMap::new, Map::clear);

    public static RegistryKey<DimensionType> getOverworldType() {
        return DimensionType.OVERWORLD;
    }

    public static SimpleRegistry<Dimension> createDimensionRegistry(long seed, DynamicRegistries registries, ChunkGenerator generator) {
        SimpleRegistry<Dimension> registry = new SimpleRegistry<>(Registry.DIMENSION_KEY, Lifecycle.stable());
        registry.register(Dimension.OVERWORLD, createDimension(getOverworldType(), registries, generator), Lifecycle.stable());
        registry.register(Dimension.THE_NETHER, createDefaultNether(seed, registries), Lifecycle.stable());
        registry.register(Dimension.THE_END, createDefaultEnd(seed, registries), Lifecycle.stable());
        return registry;
    }

    public static SimpleRegistry<Dimension> updateDimensionRegistry(SimpleRegistry<Dimension> registry, DynamicRegistries registries, ChunkGenerator generator) {
        Dimension dimension = createDimension(getOverworldType(), registries, generator);
        registry.validateAndRegister(OptionalInt.empty(), Dimension.OVERWORLD, dimension, Lifecycle.stable());
        return registry;
    }

    public static DimensionGeneratorSettings populateDimensions(DimensionGeneratorSettings level, DynamicRegistries registries, TerraSettings settings) {
        try (Resource<?> ignored = CACHE.get()) {
            // replace nether with custom dimension if present
            overrideDimension(Dimension.THE_NETHER, settings.dimensions.dimensions.nether, level, registries);
            // replace end with custom dimension if present
            overrideDimension(Dimension.THE_END, settings.dimensions.dimensions.end, level, registries);
            // scan forge registry for other level-types and add their extra (ie not overworld,nether,end) dimensions
            if (settings.dimensions.dimensions.includeExtraDimensions) {
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

    private static Dimension createDefaultNether(long seed, DynamicRegistries registries) {
        return createDefaultDimension(
                seed,
                DimensionType.THE_NETHER,
                DimensionSettings.field_242736_e,
                registries,
                NetherBiomeProvider.Preset.DEFAULT_NETHER_PROVIDER_PRESET::build
        );
    }

    private static Dimension createDefaultEnd(long seed, DynamicRegistries registries) {
        return createDefaultDimension(
                seed,
                DimensionType.THE_END,
                DimensionSettings.field_242737_f,
                registries,
                EndBiomeProvider::new
        );
    }

    private static Dimension createDefaultDimension(long seed,
                                                    RegistryKey<DimensionType> type,
                                                    RegistryKey<DimensionSettings> setting,
                                                    DynamicRegistries registries,
                                                    BiFunction<Registry<Biome>, Long, BiomeProvider> factory) {
        Registry<Biome> biomes = registries.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionSettings> settings = registries.getRegistry(Registry.NOISE_SETTINGS_KEY);
        Supplier<DimensionSettings> settingSupplier = () -> settings.getOrThrow(setting);
        BiomeProvider biomeProvider = factory.apply(biomes, seed);
        ChunkGenerator generator = new NoiseChunkGenerator(biomeProvider, seed, settingSupplier);
        return createDimension(type, registries, generator);
    }

    private static Dimension createDimension(RegistryKey<DimensionType> type, DynamicRegistries registries, ChunkGenerator generator) {
        Log.info("Creating dimension: {}", type.getLocation());
        Registry<DimensionType> types = registries.getRegistry(Registry.DIMENSION_TYPE_KEY);
        Supplier<DimensionType> typeSupplier = () -> types.getOrThrow(type);
        return new Dimension(typeSupplier, generator);
    }

    public static String getDisplayString(ForgeWorldType type) {
        if (type == LevelType.TERRAFORGED) {
            return "default";
        }
        return String.valueOf(type.getRegistryName());
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
