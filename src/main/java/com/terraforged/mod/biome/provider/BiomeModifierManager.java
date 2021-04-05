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

package com.terraforged.mod.biome.provider;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.world.biome.DesertBiomes;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.biome.modifier.BeachModifier;
import com.terraforged.engine.world.biome.modifier.BiomeModifier;
import com.terraforged.engine.world.biome.modifier.CoastModifier;
import com.terraforged.engine.world.biome.modifier.DesertColorModifier;
import com.terraforged.engine.world.biome.modifier.DesertWetlandModifier;
import com.terraforged.engine.world.biome.modifier.ModifierManager;
import com.terraforged.engine.world.biome.modifier.MountainModifier;
import com.terraforged.engine.world.biome.modifier.OceanModifier;
import com.terraforged.engine.world.biome.modifier.VolcanoModifier;
import com.terraforged.engine.world.biome.modifier.WarmLakeModifier;
import com.terraforged.engine.world.biome.modifier.WetlandModifier;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.util.DummyBlockReader;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BiomeModifierManager implements BiomeModifier {

    private final DesertBiomes desertBiomes;
    private final BiomeModifier[] modifiers;

    private BiomeModifierManager(Builder builder) {
        desertBiomes = builder.desertBiomes;
        modifiers = builder.modifiers.stream().sorted().toArray(BiomeModifier[]::new);
    }

    public boolean hasModifiers(Cell cell, Levels levels) {
        return cell.terrain.isOverground() || (cell.terrain.isSubmerged() && cell.value > levels.water);
    }

    public DesertBiomes getDesertBiomes() {
        return desertBiomes;
    }

    @Override
    public int priority() {
        return -1;
    }

    @Override
    public boolean test(int biome, Cell cell) {
        return true;
    }

    @Override
    public int modify(int biome, Cell cell, int x, int z) {
        int result;
        for (BiomeModifier modifier : modifiers) {
            if (modifier.test(biome, cell)) {
                result = modifier.modify(biome, cell, x, z);
                if (BiomeMap.isValid(result)) {
                    biome = result;
                }
                if (modifier.exitEarly()) {
                    return biome;
                }
            }
        }
        return biome;
    }

    private static BeachModifier getBeachModifier(TerraContext context, BiomeMap biomes) {
        return new BeachModifier(
                biomes,
                context,
                context.biomeContext.biomes.getId(Biomes.MUSHROOM_FIELDS),
                context.biomeContext.biomes.getId(Biomes.MUSHROOM_FIELD_SHORE)
        );
    }

    private static DesertBiomes getDesertBiomes(TFBiomeContext context, BiomeMap biomes) {
        IntList redSand = new IntArrayList();
        IntList whiteSand = new IntArrayList();
        IntList deserts = biomes.getAllBiomes(BiomeType.DESERT);
        int defaultRed = context.biomes.getId(Biomes.BADLANDS);
        int defaultWhite = context.biomes.getId(Biomes.DESERT);
        try (Resource<DummyBlockReader> reader = DummyBlockReader.pooled()) {
            for (int id : deserts) {
                if (!BiomeMap.isValid(id)) {
                    continue;
                }
                Biome biome = context.biomes.get(id);
                BiomeGenerationSettings settings = BiomeHelper.getGenSettings(biome);
                if (settings == null) {
                    continue;
                }
                ISurfaceBuilderConfig config = settings.getSurfaceBuilderConfig();
                if (config == null) {
                    continue;
                }
                BlockState top = config.getTopMaterial();
                MaterialColor color = top.getMapColor(reader.get().set(top), BlockPos.ZERO);
                int whiteDist2 = distance2(color, MaterialColor.SAND);
                int redDist2 = distance2(color, MaterialColor.CLAY);
                if (whiteDist2 < redDist2) {
                    whiteSand.add(id);
                } else {
                    redSand.add(id);
                }
            }
        }
        return new DesertBiomes(deserts, redSand, whiteSand, defaultRed, defaultWhite, biomes.getContext());
    }

    private static int distance2(MaterialColor mc1, MaterialColor mc2) {
        Color c1 = new Color(mc1.col);
        Color c2 = new Color(mc2.col);
        int dr = c1.getRed() - c2.getRed();
        int dg = c1.getGreen() - c2.getGreen();
        int db = c1.getBlue() - c2.getBlue();
        return (dr * dr) + (dg * dg) + (db * db);
    }

    public static Builder builder(TerraContext context, BiomeMap<RegistryKey<Biome>> biomes) {
        return new Builder(context, biomes);
    }

    public static class Builder implements ModifierManager {

        private final DesertBiomes desertBiomes;
        private final List<BiomeModifier> modifiers = new ArrayList<>();

        private Builder(TerraContext context, BiomeMap<RegistryKey<Biome>> biomes) {
            desertBiomes = getDesertBiomes(context.biomeContext, biomes);
            modifiers.add(new OceanModifier(context, biomes));
            modifiers.add(getBeachModifier(context, biomes));
            modifiers.add(new CoastModifier(biomes));
            modifiers.add(new DesertColorModifier(desertBiomes));
            modifiers.add(new DesertWetlandModifier(biomes));
            modifiers.add(new WetlandModifier(biomes.getContext(), ModBiomes.MARSHLAND, ModBiomes.COLD_MARSHLAND, ModBiomes.FROZEN_MARSH));
            modifiers.add(new WarmLakeModifier(biomes.getContext(), Biomes.DESERT_LAKES, ModBiomes.LAKE));
            modifiers.add(new MountainModifier(context, biomes, context.terraSettings.miscellaneous.mountainBiomeUsage));
            modifiers.add(new VolcanoModifier(biomes, context.terraSettings.miscellaneous.volcanoBiomeUsage));
        }

        @Override
        public void register(BiomeModifier modifier) {
            modifiers.add(modifier);
        }

        public BiomeModifierManager build() {
            return new BiomeModifierManager(this);
        }
    }
}
