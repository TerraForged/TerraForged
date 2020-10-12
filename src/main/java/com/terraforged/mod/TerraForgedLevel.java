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
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.settings.preset.Preset;
import com.terraforged.mod.chunk.settings.preset.PresetManager;
import com.terraforged.mod.client.gui.TerraDimGenSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
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
        return TerraChunkGenerator.createDummy(seed, biomeProvider, dimensionSettings);
    }

    @Override
    public DimensionGeneratorSettings createLevel(long seed, boolean structures, boolean chest, SimpleRegistry<Dimension> dimensions) {
        return new TerraDimGenSettings(seed, structures, chest, dimensions);
    }
}
