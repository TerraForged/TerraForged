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

import com.terraforged.api.level.type.AbstractLevelType;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.client.gui.TerraDimGenSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

public class TerraForgedLevel extends AbstractLevelType {

    public static final ResourceLocation NAME = new ResourceLocation(TerraForgedMod.MODID, "terraforged");

    @Override
    public ChunkGenerator create(long seed, Registry<Biome> biomes, Registry<DimensionSettings> settings) {
        TerraBiomeProvider biomeProvider = TerraBiomeProvider.create(seed, biomes);
        DimensionSettings dimensionSettings = settings.func_243576_d(DimensionSettings.field_242734_c);
        return new TerraChunkGenerator(biomeProvider, dimensionSettings);
    }

    @Override
    public DimensionGeneratorSettings createLevel(long seed, boolean structures, boolean chest, SimpleRegistry<Dimension> dimensions) {
        return new TerraDimGenSettings(seed, structures, chest, dimensions);
    }

    public static DimensionGeneratorSettings updateOverworld(DimensionGeneratorSettings level, DynamicRegistries registries, TerraSettings settings) {
        return new DimensionGeneratorSettings(
                level.getSeed(),
                level.doesGenerateFeatures(),
                level.hasBonusChest(),
                DimensionGeneratorSettings.func_241520_a_(
                        level.func_236224_e_(),
                        () -> registries.func_230520_a_().func_243576_d(DimensionType.OVERWORLD),
                        createOverworld(registries, settings)
                )
        );
    }

    private static TerraChunkGenerator createOverworld(DynamicRegistries registries, TerraSettings settings) {
        Registry<DimensionSettings> dimSettings = registries.func_243612_b(Registry.field_243549_ar);
        DimensionSettings overworldSettings = dimSettings.func_243576_d(DimensionSettings.field_242734_c);
        GameContext game = new GameContext(registries.func_243612_b(Registry.BIOME_KEY));
        TerraContext context = new TerraContext(settings, game);
        TerraBiomeProvider biomeProvider = new TerraBiomeProvider(context);
        return new TerraChunkGenerator(biomeProvider, overworldSettings);
    }
}
