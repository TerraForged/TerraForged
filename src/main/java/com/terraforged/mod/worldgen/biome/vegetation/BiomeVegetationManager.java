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

import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.asset.ViabilityConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;

import java.util.IdentityHashMap;
import java.util.Map;

public class BiomeVegetationManager {
    private final Map<Biome, BiomeVegetation> vegetation = new IdentityHashMap<>();
    private final Map<Biome, ViabilityConfig> viability = new IdentityHashMap<>();

    public BiomeVegetationManager(RegistryAccess access) {
        var biomes = access.registryOrThrow(Registry.BIOME_REGISTRY);
        var viabilities = access.registryOrThrow(ModRegistry.VIABILITY);
        for (var entry : biomes.entrySet()) {
            var biome = entry.getValue();
            var vegetation = new BiomeVegetation(entry.getKey(), access);
            var viability = getViability(biome, viabilities);

            this.vegetation.put(biome, vegetation);

            if (viability != null) {
                this.viability.put(biome, viability);
            }
        }
    }

    public BiomeVegetation getVegetation(Biome biome) {
        return vegetation.getOrDefault(biome, BiomeVegetation.NONE);
    }

    public ViabilityConfig getViability(Biome biome) {
        return viability.getOrDefault(biome, ViabilityConfig.NONE);
    }

    private static ViabilityConfig getViability(Biome biome, Registry<ViabilityConfig> registry) {
        return registry.stream().filter(vc -> vc.biomes().get().contains(biome)).findFirst().orElse(null);
    }
}
