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

package com.terraforged.mod.worldgen.cave;

import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.NoiseCaveConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Random;

public class NoiseCaveGenerator {
    protected final NoiseCaveConfig[] configs;

    public NoiseCaveGenerator(long seed, RegistryAccess access) {
        this(access.registryOrThrow(ModRegistry.NOISE_CAVE));
    }

    public NoiseCaveGenerator(Registry<NoiseCaveConfig> registry) {
        this(registry.stream().toArray(NoiseCaveConfig[]::new));
    }

    public NoiseCaveGenerator(NoiseCaveConfig[] configs) {
        this.configs = configs;
    }

    public void carve(ChunkAccess chunk, Generator generator) {
        for (var config : configs) {
            NoiseCaveCarver.carve(chunk, generator, config);
        }
    }

    public void decorate(ChunkAccess chunk, WorldGenLevel region, Generator generator) {
        for (var config : configs) {
            NoiseCaveDecorator.decorate(chunk, region, generator, config);
        }
    }

    private static NoiseCaveConfig[] createDummyConfigs(long seed, RegistryAccess access) {
        var random = new Random(seed);
        var registry = access.registryOrThrow(Registry.BIOME_REGISTRY);
        return new NoiseCaveConfig[]{
                NoiseCaveConfig.create0(random.nextInt(), Biomes.LUSH_CAVES, registry::getOrThrow),
                NoiseCaveConfig.create1(random.nextInt(), Biomes.DRIPSTONE_CAVES, registry::getOrThrow),
        };
    }
}
