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

package com.terraforged.mod;

import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.util.DataUtils;
import com.terraforged.mod.util.DimUtils;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType;

public class LevelType implements ForgeWorldType.IChunkGeneratorFactory {

    public static final ForgeWorldType TERRAFORGED = new ForgeWorldType(new LevelType())
            .setRegistryName(TerraForgedMod.MODID, "terraforged");

    @Override
    public TFChunkGenerator createChunkGenerator(Registry<Biome> biomes, Registry<DimensionSettings> settings, long seed, String options) {
        Log.info("Creating default TerraForged chunk-generator");
        return createOverworld(TerraSettings.defaults(seed), biomes, settings);
    }

    @Override
    public DimensionGeneratorSettings createSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateStructures, boolean bonusChest, String options) {
        Log.info("Creating TerraForged level settings");
        Registry<Biome> biomes = dynamicRegistries.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionType> dimensions = dynamicRegistries.getRegistry(Registry.DIMENSION_TYPE_KEY);
        Registry<DimensionSettings> settings = dynamicRegistries.getRegistry(Registry.NOISE_SETTINGS_KEY);
        TFChunkGenerator chunkGenerator = createChunkGenerator(biomes, settings, seed, options);
        DimensionGeneratorSettings level = new DimensionGeneratorSettings(
                seed,
                generateStructures,
                bonusChest,
                DimensionGeneratorSettings.func_242749_a(
                        dimensions,
                        DimensionType.getDefaultSimpleRegistry(dimensions, biomes, settings, seed),
                        chunkGenerator
                )
        );
        return DimUtils.populateDimensions(level, dynamicRegistries, chunkGenerator.getContext().terraSettings.dimensions);
    }

    public static DimensionGeneratorSettings updateOverworld(DimensionGeneratorSettings level, DynamicRegistries registries, TerraSettings settings) {
        Log.info("Updating TerraForged level settings");
        TFChunkGenerator updatedGenerator = createOverworld(settings, registries);
        DimensionGeneratorSettings updatedLevel = new DimensionGeneratorSettings(
                level.getSeed(),
                level.doesGenerateFeatures(),
                level.hasBonusChest(),
                DimensionGeneratorSettings.func_241520_a_(
                        level.func_236224_e_(),
                        () -> registries.func_230520_a_().getOrThrow(DimensionType.OVERWORLD),
                        updatedGenerator
                )
        );
        return DimUtils.populateDimensions(updatedLevel, registries, settings.dimensions);
    }

    private static TFChunkGenerator createOverworld(TerraSettings settings, DynamicRegistries registries) {
        return createOverworld(settings, registries.getRegistry(Registry.BIOME_KEY), registries.getRegistry(Registry.NOISE_SETTINGS_KEY));
    }

    private static TFChunkGenerator createOverworld(TerraSettings settings, Registry<Biome> biomes, Registry<DimensionSettings> dimSettings) {
        Log.info("Creating TerraForged chunk-generator with settings {}", DataUtils.toJson(settings));
        DimensionSettings overworldSettings = dimSettings.getOrThrow(DimensionSettings.field_242734_c);
        TFBiomeContext game = new TFBiomeContext(biomes);
        TerraContext context = new TerraContext(settings, game);
        TFBiomeProvider biomeProvider = new TFBiomeProvider(context);
        return new TFChunkGenerator(biomeProvider, overworldSettings);
    }
}
