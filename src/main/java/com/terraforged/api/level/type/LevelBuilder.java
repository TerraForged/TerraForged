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

package com.terraforged.api.level.type;

import com.mojang.serialization.Lifecycle;
import com.terraforged.api.level.type.LevelType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

import java.util.OptionalInt;
import java.util.function.Supplier;

public class LevelBuilder {

    private final LevelType type;
    private final long seed;
    private final boolean structures;
    private final boolean chest;
    private final Registry<DimensionType> types;
    private final Registry<Biome> biomes;
    private final Registry<DimensionSettings> settings;
    private final SimpleRegistry<Dimension> dimensions;

    public LevelBuilder(LevelType type, long seed, boolean structures, boolean chest, Registry<DimensionType> types, Registry<Biome> biomes, Registry<DimensionSettings> settings, SimpleRegistry<Dimension> dimensions) {
        this.type = type;
        this.seed = seed;
        this.chest = chest;
        this.structures = structures;
        this.types = types;
        this.biomes = biomes;
        this.settings = settings;
        this.dimensions = dimensions;
    }

    public LevelBuilder(LevelType type, long seed, boolean structures, boolean chest, Registry<DimensionType> types, Registry<Biome> biomes, Registry<DimensionSettings> settings) {
        this(type, seed, structures, chest, types, biomes, settings, DimensionType.func_242718_a(types, biomes, settings, seed));
    }

    public long getSeed() {
        return seed;
    }

    public boolean hasChest() {
        return chest;
    }

    public boolean hasStructures() {
        return structures;
    }

    public Supplier<DimensionType> getType(RegistryKey<DimensionType> key) {
        return () -> types.func_243576_d(key);
    }

    public Registry<Biome> getBiomes() {
        return biomes;
    }

    public Registry<DimensionSettings> getSettings() {
        return settings;
    }

    public void add(RegistryKey<Dimension> key, Dimension dimension) {
        dimensions.func_241874_a(OptionalInt.empty(), key, dimension, Lifecycle.stable());
    }

    public DimensionGeneratorSettings build() {
        return type.createLevel(getSeed(), hasStructures(), hasChest(), dimensions);
    }
}
