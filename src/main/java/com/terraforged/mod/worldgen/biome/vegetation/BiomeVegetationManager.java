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

package com.terraforged.mod.worldgen.biome.vegetation;

import com.terraforged.mod.data.ModVegetations;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;

import java.util.IdentityHashMap;
import java.util.Map;

public class BiomeVegetationManager {
    private final Map<Biome, BiomeVegetation> vegetation = new IdentityHashMap<>();

    public BiomeVegetationManager(RegistryAccess access) {
        var biomes = access.registryOrThrow(Registry.BIOME_REGISTRY);
        var configs = ModVegetations.getVegetation(access);

        for (var entry : biomes.entrySet()) {
            var biome = entry.getValue();
            var config = getConfig(biome, configs);
            var features = VegetationFeatures.create(entry.getKey(), access, config);
            this.vegetation.put(biome, new BiomeVegetation(config, features));
        }
    }

    public BiomeVegetation getVegetation(Biome biome) {
        return vegetation.get(biome);
    }

    private static VegetationConfig getViability(Biome biome, Registry<VegetationConfig> registry) {
        return registry.stream().filter(vc -> vc.biomes().get().contains(biome)).findFirst().orElse(VegetationConfig.NONE);
    }

    private static VegetationConfig[] getConfigs(RegistryAccess access) {
        return ModVegetations.getVegetation(access);
    }

    private static VegetationConfig getConfig(Biome biome, VegetationConfig[] configs) {
        for (var config : configs) {
            if (config.biomes().get().contains(biome)) {
                return config;
            }
        }
        return VegetationConfig.NONE;
    }
}
