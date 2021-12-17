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

import com.google.common.base.Suppliers;
import com.terraforged.engine.Seed;
import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.ModRegistries;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.util.seed.RandSeed;
import com.terraforged.mod.worldgen.asset.BiomeTag;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.biome.viability.*;
import com.terraforged.noise.Source;
import net.minecraft.core.RegistryAccess;

import java.util.function.Supplier;

public interface ModVegetation extends ModRegistry {
    static void register() {
        var seed = Factory.createSeed();
        ModRegistries.register(VEGETATION, "trees_copse", Factory.copse(seed, null));
        ModRegistries.register(VEGETATION, "trees_sparse", Factory.sparse(seed, null));
        ModRegistries.register(VEGETATION, "trees_patchy", Factory.patchy(seed, null));
        ModRegistries.register(VEGETATION, "trees_temperate", Factory.temperate(seed, null));
        ModRegistries.register(VEGETATION, "trees_hardy", Factory.hardy(seed, null));
        ModRegistries.register(VEGETATION, "trees_rainforest", Factory.jungle(seed, null));
        ModRegistries.register(VEGETATION, "trees_sparse_rainforest", Factory.sparseJungle(seed, null));
    }

    static VegetationConfig[] getVegetation(RegistryAccess access) {
        if (access == null) {
            return Factory.getDefaults(null);
        }

        if (Environment.DEV_ENV) {
            return Factory.getDefaults(access);
        }

        return access.ownedRegistryOrThrow(ModRegistry.VEGETATION.get()).stream().toArray(VegetationConfig[]::new);
    }

    class Factory {
        static Seed createSeed() {
            return new RandSeed(2353245L, 500_000);
        }

        static VegetationConfig copse(Seed seed, RegistryAccess access) {
            return new VegetationConfig(0.25F, 0.8F, 0.65F, tag("trees/copses", access), SumViability.builder(0F)
                    .with(0.2F, new SaturationViability(0.7F, 1F))
                    .with(-1.0F, new HeightViability(-40, 30, 150))
                    .with(-1.0F, new SlopeViability(80, 0.75F))
                    .with(1.0F, new NoiseViability(Source.simplex(seed.next(), 120, 2).clamp(0.75, 0.9F).map(0, 1)))
                    .build());
        }

        static VegetationConfig hardy(Seed seed, RegistryAccess access) {
            return new VegetationConfig(0.32F, 0.8F, 0.7F, tag("trees/hardy", access), SumViability.builder(0.9F)
                    .with(0.3F, new SaturationViability(0.85F, 1F))
                    .with(-1.0F, new HeightViability(-100, 40, 185))
                    .with(-1.0F, new SlopeViability(75, 0.8F))
                    .with(-0.75F, new BiomeEdgeViability(0.65F))
                    .with(-0.4F, new NoiseViability(Source.simplex(seed.next(), 120, 2).clamp(0.4, 0.8).map(0, 1)))
                    .build());
        }

        static VegetationConfig sparse(Seed seed, RegistryAccess access) {
            return new VegetationConfig(0.20F, 0.75F, 0.6F, tag("trees/sparse", access), SumViability.builder(0F)
                    .with(0.25F, new SaturationViability(0.7F, 1F))
                    .with(-1.0F, new HeightViability(-100, 50, 145))
                    .with(-1.0F, new SlopeViability(80, 0.65F))
                    .with(1F, new NoiseViability(Source.simplex(seed.next(), 120, 2).clamp(0.75, 0.9).map(0, 1)))
                    .build());
        }

        static VegetationConfig jungle(Seed seed, RegistryAccess access) {
            return new VegetationConfig(0.35F, 0.75F, 0.7F, tag("trees/rainforest", access), SumViability.builder(0.9F)
                    .with(0.35F, new SaturationViability(0.85F, 1F))
                    .with(-1.0F, new HeightViability(-100, 40, 185))
                    .with(-1.0F, new SlopeViability(75, 0.8F))
                    .with(-0.95F, new BiomeEdgeViability(0.75F))
                    .with(-0.2F, new NoiseViability(Source.simplex(seed.next(), 150, 2).clamp(0.6, 0.9).map(0, 1)))
                    .build());
        }

        static VegetationConfig sparseJungle(Seed seed, RegistryAccess access) {
            return new VegetationConfig(0.18F, 0.85F, 0.5F, tag("trees/sparse_rainforest", access), SumViability.builder(0.25F)
                    .with(0.3F, new SaturationViability(0.65F, 1F))
                    .with(-1.0F, new HeightViability(-10, 30, 150))
                    .with(-1.0F, new SlopeViability(80, 0.75F))
                    .with(0.8F, new NoiseViability(Source.simplex(seed.next(), 80, 2).clamp(0.3, 0.7).map(0, 1)))
                    .build());
        }

        static VegetationConfig temperate(Seed seed, RegistryAccess access) {
            return new VegetationConfig(0.3F, 0.8F, 0.65F, tag("trees/temperate", access), SumViability.builder(0.8F)
                    .with(0.25F, new SaturationViability(0.95F, 1F))
                    .with(-1.0F, new HeightViability(-100, 30, 150))
                    .with(-1.0F, new SlopeViability(70, 0.65F))
                    .with(-0.8F, new BiomeEdgeViability(0.7F))
                    .with(-0.3F, new NoiseViability(Source.simplex(seed.next(), 100, 2).clamp(0.3, 0.7).map(0, 1)))
                    .build());
        }

        static VegetationConfig patchy(Seed seed, RegistryAccess access) {
            return new VegetationConfig(0.34F, 0.8F, 0.75F, tag("trees/patchy", access), SumViability.builder(0.75F)
                    .with(0.3F, new SaturationViability(0.97F, 1F))
                    .with(-1.0F, new HeightViability(-100, 30, 150))
                    .with(-1.0F, new SlopeViability(70, 0.65F))
                    .with(-0.8F, new BiomeEdgeViability(0.7F))
                    .with(-0.4F, new NoiseViability(Source.simplex(seed.next(), 130, 3).clamp(0.3, 0.7).map(0, 1)))
                    .build());
        }

        static Supplier<BiomeTag> tag(String name, RegistryAccess access) {
            if (access == null) {
                return ModRegistries.supplier(ModRegistry.BIOME_TAG.get(), name);
            } else {
                var tags = access.ownedRegistryOrThrow(BIOME_TAG.get());
                return Suppliers.ofInstance(tags.get(TerraForged.location(name)));
            }
        }

        static VegetationConfig[] getDefaults(RegistryAccess access) {
            var seed = new RandSeed(2353245L, 500_000);
            return new VegetationConfig[] {
                    copse(seed, access),
                    hardy(seed, access),
                    sparse(seed, access),
                    jungle(seed, access),
                    sparseJungle(seed, access),
                    temperate(seed, access),
                    patchy(seed, access),
            };
        }
    }
}
