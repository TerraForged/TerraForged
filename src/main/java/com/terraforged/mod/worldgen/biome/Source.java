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

package com.terraforged.mod.worldgen.biome;

import com.mojang.serialization.Codec;
import com.terraforged.engine.concurrent.cache.map.LoadBalanceLongMap;
import com.terraforged.engine.concurrent.cache.map.LongMap;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.mod.worldgen.biome.util.BiomeUtil;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

import java.util.List;

public class Source extends BiomeSource {
    public static final Codec<Source> CODEC = new SourceCodec();

    protected final long seed;
    protected final BiomeGenerator biomeGenerator;
    protected final Registry<Biome> registry;
    protected final LongMap<Biome> cache = creatBiomeCache();

    public Source(long seed, NoiseGenerator noise, Source other) {
        super(List.copyOf(other.possibleBiomes()));
        this.seed = seed;
        this.registry = other.registry;
        this.biomeGenerator = new BiomeGenerator(noise, other.registry, List.copyOf(other.possibleBiomes()));
    }

    public Source(long seed, NoiseGenerator noise, Registry<Biome> biomes) {
        this(seed, noise, biomes, BiomeUtil.getOverworldBiomes(biomes));
    }

    public Source(long seed, NoiseGenerator noise, Registry<Biome> registry, List<Biome> biomes) {
        super(biomes);
        this.seed = seed;
        this.registry = registry;
        this.biomeGenerator = new BiomeGenerator(noise, registry, biomes);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Source withSeed(long l) {
        return this;
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return cache.computeIfAbsent(PosUtil.pack(x, z), this::compute);
    }

    public Biome getCarvingBiome() {
        return registry.getOrThrow(Biomes.LUSH_CAVES);
    }

    public Registry<Biome> getRegistry() {
        return registry;
    }

    protected Biome compute(long index) {
        int x = PosUtil.unpackLeft(index) << 2;
        int z = PosUtil.unpackRight(index) << 2;
        return biomeGenerator.generate(x, z);
    }

    protected static LongMap<Biome> creatBiomeCache() {
        int factor = Runtime.getRuntime().availableProcessors();
        return new LoadBalanceLongMap<>(factor, 1024);
    }

    public static class NoopSampler implements Climate.Sampler {
        public static final NoopSampler INSTANCE = new NoopSampler();
        public static final Climate.TargetPoint DEFAULT_POINT = new Climate.TargetPoint(0, 0, 0, 0, 0, 0);

        @Override
        public Climate.TargetPoint sample(int i, int i1, int i2) {
            return DEFAULT_POINT;
        }
    }
}
