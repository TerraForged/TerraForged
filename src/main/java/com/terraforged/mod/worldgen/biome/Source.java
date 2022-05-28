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
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.mod.util.map.LongCache;
import com.terraforged.mod.util.map.LossyCache;
import com.terraforged.mod.worldgen.biome.util.BiomeMapManager;
import com.terraforged.mod.worldgen.cave.CaveType;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.Set;

public class Source extends BiomeSource {
    public static final Codec<Source> CODEC = new SourceCodec();
    public static final Climate.Sampler NOOP_CLIMATE_SAMPLER = Climate.empty();

    protected final long seed;
    protected final RegistryAccess registries;
    protected final Set<Holder<Biome>> possibleBiomes;
    protected final BiomeSampler biomeSampler;
    protected final BiomeMapManager biomeMapManager;
    protected final CaveBiomeSampler caveBiomeSampler;
    protected final LongCache<Holder<Biome>> cache = LossyCache.concurrent(2048, i -> (Holder<Biome>[]) new Holder[i]);

    public Source(long seed, INoiseGenerator noise, Source other) {
        super(List.of());
        this.seed = seed;
        this.registries = other.registries;
        this.biomeMapManager = other.biomeMapManager;
        this.possibleBiomes = new ObjectLinkedOpenHashSet<>(other.possibleBiomes);
        this.biomeSampler = new BiomeSampler(noise, other.biomeMapManager);
        this.caveBiomeSampler = new CaveBiomeSampler(seed, other.caveBiomeSampler);
    }

    public Source(long seed, INoiseGenerator noise, RegistryAccess access) {
        super(List.of());
        this.seed = seed;
        this.registries = access;
        this.biomeMapManager = new BiomeMapManager(access);
        this.possibleBiomes = new ObjectLinkedOpenHashSet<>(biomeMapManager.getOverworldBiomes());
        this.biomeSampler = new BiomeSampler(noise, biomeMapManager);
        this.caveBiomeSampler = new CaveBiomeSampler(seed, 800, biomeMapManager);
    }

    /**
     * Note: We provide the super-class an empty list to avoid the biome feature
     * order dependency exceptions (wtf mojang). We do not use the featuresByStep
     * list so order does not matter to us (thank god! Biome mods are going to
     * get this very wrong).
     * <p>
     * We instead maintain our own set with the actual biomes and override here :)
     */
    @Override
    public Set<Holder<Biome>> possibleBiomes() {
        return possibleBiomes;
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
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if (true) return compute(PosUtil.pack(x, z));

        return cache.computeIfAbsent(PosUtil.pack(x, z), this::compute);
    }

    public RegistryAccess getRegistries() {
        return registries;
    }

    public BiomeSampler getBiomeSampler() {
        return biomeSampler;
    }

    public CaveBiomeSampler getCaveBiomeSampler() {
        return caveBiomeSampler;
    }

    public Holder<Biome> getUnderGroundBiome(int seed, int x, int z, CaveType type) {
        return caveBiomeSampler.getUnderGroundBiome(seed, x, z, type);
    }

    public Registry<Biome> getRegistry() {
        return biomeMapManager.getBiomes();
    }

    protected Holder<Biome> compute(long index) {
        int x = PosUtil.unpackLeft(index) << 2;
        int z = PosUtil.unpackRight(index) << 2;
        return biomeSampler.sampleBiome(x, z);
    }
}
