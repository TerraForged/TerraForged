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

import com.terraforged.mod.worldgen.biome.surface.SurfaceChunk;
import com.terraforged.mod.worldgen.biome.surface.SurfaceRegion;
import net.minecraft.core.Registry;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.function.Supplier;

public class VanillaGen {
    protected final ChunkGenerator vanillaGenerator;
    protected final Supplier<NoiseGeneratorSettings> settings;
    protected final Registry<NormalNoise.NoiseParameters> parameters;

    public VanillaGen(long seed, BiomeSource biomeSource, VanillaGen other) {
        this(seed, biomeSource, other.settings, other.parameters);
    }

    public VanillaGen(long seed, BiomeSource biomeSource, Supplier<NoiseGeneratorSettings> settings, Registry<NormalNoise.NoiseParameters> parameters) {
        this.settings = settings;
        this.parameters = parameters;
        this.vanillaGenerator = new NoiseBasedChunkGenerator(parameters, biomeSource, seed, settings);
    }

    public ChunkGenerator getVanillaGenerator() {
        return vanillaGenerator;
    }

    public void buildSurface(WorldGenRegion region, StructureFeatureManager structures, ChunkAccess chunk, Generator generator) {
        region = SurfaceRegion.wrap(region);
        chunk = SurfaceChunk.assign(chunk, generator.getChunkDataAsync(chunk.getPos()));
        vanillaGenerator.buildSurface(region, structures, chunk);
    }
}
