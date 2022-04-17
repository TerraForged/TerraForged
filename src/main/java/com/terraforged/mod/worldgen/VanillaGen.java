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

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class VanillaGen {
    protected final NoiseRouter noiseRouter;
    protected final Registry<StructureSet> structureSets;
    protected final NoiseBasedChunkGenerator vanillaGenerator;
    protected final Holder<NoiseGeneratorSettings> settings;
    protected final Registry<NormalNoise.NoiseParameters> parameters;

    protected final int lavaLevel;
    protected final Aquifer.FluidStatus fluidStatus1;
    protected final Aquifer.FluidStatus fluidStatus2;
    protected final Aquifer.FluidPicker globalFluidPicker;

    public VanillaGen(long seed, BiomeSource biomeSource, VanillaGen other) {
        this(seed, biomeSource, other.settings, other.parameters, other.structureSets);
    }

    public VanillaGen(long seed, BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, Registry<NormalNoise.NoiseParameters> parameters, Registry<StructureSet> structures) {
        this.settings = settings;
        this.parameters = parameters;
        this.structureSets = structures;
        this.vanillaGenerator = new NoiseBasedChunkGenerator(structures, parameters, biomeSource, seed, settings);
        this.noiseRouter = settings.value().createNoiseRouter(parameters, seed);
        this.lavaLevel = Math.min(-54, settings.value().seaLevel());
        this.fluidStatus1 = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        this.fluidStatus2 = new Aquifer.FluidStatus(settings.value().seaLevel(), settings.value().defaultFluid());
        this.globalFluidPicker = (x, y, z) -> y < lavaLevel ? fluidStatus1 : fluidStatus2;
    }

    public Holder<NoiseGeneratorSettings> getSettings() {
        return settings;
    }

    public Registry<StructureSet> getStructureSets() {
        return structureSets;
    }

    public NoiseRouter getNoiseRouter() {
        return noiseRouter;
    }

//    public StructureSettings getStructureSettings() {
//        return new StructureConfig(getSettings().get().structureSettings()).copy();
//    }

    public Aquifer.FluidPicker getGlobalFluidPicker() {
        return globalFluidPicker;
    }

    public ChunkGenerator getVanillaGenerator() {
        return vanillaGenerator;
    }

    public ChunkGenerator createStructureGenerator(long seed, Generator generator) {
        return null;
//        return new DelegateGenerator(seed, generator, settings) {};
    }

    public CarvingContext createCarvingContext(WorldGenRegion region, ChunkAccess chunk, NoiseChunk noiseChunk) {
        return new CarvingContext(vanillaGenerator, region.registryAccess(), chunk.getHeightAccessorForGeneration(), noiseChunk);
    }
}
