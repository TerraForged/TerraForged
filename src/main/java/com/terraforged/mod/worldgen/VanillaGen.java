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

package com.terraforged.mod.worldgen;

import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.function.Supplier;

public class VanillaGen {
    protected final NoiseSampler noiseSampler;
    protected final NoiseBasedChunkGenerator vanillaGenerator;
    protected final Supplier<NoiseGeneratorSettings> settings;
    protected final Registry<NormalNoise.NoiseParameters> parameters;

    protected final int lavaLevel;
    protected final Aquifer.FluidStatus fluidStatus1;
    protected final Aquifer.FluidStatus fluidStatus2;
    protected final Aquifer.FluidPicker globalFluidPicker;

    public VanillaGen(long seed, BiomeSource biomeSource, VanillaGen other) {
        this(seed, biomeSource, other.settings, other.parameters);
    }

    public VanillaGen(long seed, BiomeSource biomeSource, Supplier<NoiseGeneratorSettings> settings, Registry<NormalNoise.NoiseParameters> parameters) {
        this.settings = settings;
        this.parameters = parameters;
        this.vanillaGenerator = new NoiseBasedChunkGenerator(parameters, biomeSource, seed, settings);
        this.noiseSampler = new NoiseSampler(settings.get().noiseSettings(), settings.get().isNoiseCavesEnabled(), seed, parameters, settings.get().getRandomSource());
        this.lavaLevel = Math.min(-54, settings.get().seaLevel());
        this.fluidStatus1 = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        this.fluidStatus2 = new Aquifer.FluidStatus(settings.get().seaLevel(), settings.get().getDefaultFluid());
        this.globalFluidPicker = (x, y, z) -> y < lavaLevel ? fluidStatus1 : fluidStatus2;
    }

    public Aquifer.FluidPicker getGlobalFluidPicker() {
        return globalFluidPicker;
    }

    public NoiseSampler getNoiseSampler() {
        return noiseSampler;
    }

    public Supplier<NoiseGeneratorSettings> getSettings() {
        return settings;
    }

    public ChunkGenerator getVanillaGenerator() {
        return vanillaGenerator;
    }

    public CarvingContext createCarvingContext(WorldGenRegion region, ChunkAccess chunk, NoiseChunk noiseChunk) {
        return new CarvingContext(vanillaGenerator, region.registryAccess(), chunk.getHeightAccessorForGeneration(), noiseChunk);
    }
}
