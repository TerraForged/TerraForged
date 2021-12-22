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

package com.terraforged.mod.data;

import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.registry.ModRegistries;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.asset.ClimateType;
import com.terraforged.mod.worldgen.biome.util.BiomeUtil;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.List;
import java.util.Locale;

public interface ModClimates extends ModRegistry {
    float RARE = 1F;
    float NORMAL = 5F;

    static void register() {
        var registry = BuiltinRegistries.BIOME;
        var biomes = BiomeUtil.getOverworldBiomes(registry);
        for (var type : BiomeType.values()) {
            ModRegistries.register(CLIMATE, type.name().toLowerCase(Locale.ROOT), Factory.create(type, biomes, registry));
        }
    }

    class Factory {
        static ClimateType create(BiomeType type, List<Biome> biomes, Registry<Biome> registry) {
            var weights = new Object2FloatOpenHashMap<ResourceLocation>();

            for (var biome : biomes) {
                var biomeType = BiomeUtil.getType(biome);
                if (biomeType == null || biomeType != type) continue;

                var key = registry.getResourceKey(biome).orElseThrow();
                weights.put(key.location(), getWeight(key, biome));
            }

            return new ClimateType(weights);
        }

        static float getWeight(ResourceKey<Biome> key, Biome biome) {
            if (biome.getBiomeCategory() == Biome.BiomeCategory.MUSHROOM) return RARE;
            if (key == Biomes.ICE_SPIKES) return RARE;
            return NORMAL;
        }
    }
}
