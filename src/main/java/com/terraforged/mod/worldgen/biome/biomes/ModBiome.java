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

package com.terraforged.mod.worldgen.biome.biomes;

import com.terraforged.mod.TerraForged;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record ModBiome(ResourceKey<Biome> key, Supplier<Biome> factory) {
    public Biome create() {
        return factory.get();
    }

    public static ModBiome of(String name, ResourceKey<Biome> parent, Consumer<Biome.BiomeBuilder> modifier) {
        var key = ResourceKey.create(Registry.BIOME_REGISTRY, TerraForged.location(name));
        var factory = copyFactory(parent, modifier);
        return new ModBiome(key, factory);
    }

    private static Supplier<Biome> copyFactory(ResourceKey<Biome> parent, Consumer<Biome.BiomeBuilder> modifier) {
        return () -> {
            var builder = builderOf(parent);
            modifier.accept(builder);
            return builder.build();
        };
    }

    private static Biome.BiomeBuilder builderOf(ResourceKey<Biome> parent) {
        var biome = BuiltinRegistries.BIOME.getOrThrow(parent);
        var holder = BuiltinRegistries.BIOME.getHolderOrThrow(parent);
        var builder = new Biome.BiomeBuilder();
        builder.downfall(biome.getDownfall());
        builder.biomeCategory(Biome.getBiomeCategory(holder));
        builder.temperature(biome.getBaseTemperature());
        builder.mobSpawnSettings(biome.getMobSettings());
        builder.precipitation(biome.getPrecipitation());
        builder.specialEffects(biome.getSpecialEffects());
        builder.generationSettings(biome.getGenerationSettings());
        return builder;
    }
}
