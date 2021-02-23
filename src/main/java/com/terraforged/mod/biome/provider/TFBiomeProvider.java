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

package com.terraforged.mod.biome.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.task.LazySupplier;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.heightmap.WorldLookup;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.provider.analyser.BiomeAnalyser;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public class TFBiomeProvider extends BiomeProvider {

    public static final Codec<TFBiomeProvider> CODEC = Codecs.create(TFBiomeProvider::encode, TFBiomeProvider::decode);

    private final long seed;
    private final TerraContext context;
    private final BiomeCache biomeCache;
    private final LazySupplier<BiomeResources> resources;

    private final float biomeSearchModifier;

    public TFBiomeProvider(TerraContext context) {
        super(BiomeAnalyser.getOverworldBiomesList(context.biomeContext));
        this.context = context;
        this.seed = context.terraSettings.world.seed;
        this.biomeCache = new BiomeCache(this);
        this.resources = LazySupplier.factory(context.copy(), BiomeResources::new);
        this.biomeSearchModifier = BiomeHelper.getBiomeSizeSearchModifier(context.settings.climate);
    }

    @Override
    protected Codec<TFBiomeProvider> getBiomeProviderCodec() {
        return CODEC;
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        try (Resource<Cell> resource = Cell.pooled()) {
            Cell cell = resource.get().reset();
            return getNoiseBiome(cell, x, z);
        }
    }

    @Override
    public TFBiomeProvider getBiomeProvider(long seed) {
        Log.debug("Creating seeded biome provider: {}", seed);
        TerraSettings settings = context.terraSettings;
        settings.world.seed = seed;
        return new TFBiomeProvider(new TerraContext(settings, context.biomeContext));
    }

    @Override
    public Set<Biome> getBiomes(int centerX, int centerY, int centerZ, int radius) {
        // search smaller radius to encourage more attempts to generate structures like mansions
        radius = Math.max(8, NoiseUtil.round(biomeSearchModifier * radius));
        int minX = centerX - radius;
        int minZ = centerZ - radius;
        int maxX = centerX + radius;
        int maxZ = centerZ + radius;
        Set<Biome> set = Sets.newHashSet();
        Cell cell = new Cell();
        for (int z = minZ; z <= maxZ; z += 4) {
            for (int x = minX; x <= maxX; x += 4) {
                Biome biome = lookupBiome(cell, x, z, false);
                set.add(biome);
            }
        }
        return set;
    }

    @Override
    @Nullable
    public BlockPos findBiomePosition(int centerX, int centerY, int centerZ, int radius, int increment, Predicate<Biome> biomes, Random random, boolean centerOutSearch) {
        // convert block coords to biome coords
        int biomeRadius = radius >> 2;
        int biomeCenterX = centerX >> 2;
        int biomeCenterZ = centerZ >> 2;

        Cell cell = new Cell();
        BlockPos.Mutable pos = null;

        // keeps track of the number of matched positions and progressively reduces the likelihood of a matching position
        // being selected as the result
        int count = 0;

        // centerOutSearch iterates concentric rings around the center coordinates to find the closest matching position
        // non-centerOut iterates the entire square around the center and returns a random matching position
        int startRadius = centerOutSearch ? 0 : biomeRadius;

        for (int r = startRadius; r < biomeRadius; r += increment) {
            for (int dz = -r; dz <= r; dz++) {
                boolean onRadiusZ = Math.abs(dz) == r;

                for (int dx = -r; dx <= r; dx++) {
                    if (centerOutSearch) {
                        boolean onRadiusX = Math.abs(dx) == r;
                        if (!onRadiusX && !onRadiusZ) {
                            continue;
                        }
                    }

                    int biomeX = biomeCenterX + dx;
                    int biomeZ = biomeCenterZ + dz;

                    // getBiome(Cell,int,int) expects block coords, not biome coords
                    int x = biomeX << 2;
                    int z = biomeZ << 2;

                    Biome biome = lookupBiome(cell, x, z, false);

                    if (biomes.test(biome)) {
                        if (centerOutSearch) {
                            return new BlockPos(x, centerY, z);
                        }

                        if (pos == null) {
                            pos = new BlockPos.Mutable(x, centerY, z);
                        } else if (random.nextInt(count + 1) == 0) {
                            // as the match count increases the chance of getting a zero reduces
                            pos.setPos(x, centerY, z);
                        }

                        count++;
                    }
                }
            }
        }
        return pos;
    }

    public WorldLookup getWorldLookup() {
        return getResources().worldLookup;
    }

    public TerraContext getContext() {
        return context;
    }

    public TerraSettings getSettings() {
        return context.terraSettings;
    }

    public BiomeModifierManager getModifierManager() {
        return getResources().modifierManager;
    }

    public Biome getNoiseBiome(Cell cell, int x, int z) {
        int biome = biomeCache.getNoiseBiome(cell, x, z, false);
        return getBiomeFromId(biome);
    }

    public Biome lookupBiome(Cell cell, int x, int z, boolean load) {
        int biomeId = biomeCache.tryGetBiome(cell, x, z, load);
        if (!BiomeMap.isValid(biomeId)) {
            biomeId = computeBiome(cell, x, z, load);
            biomeCache.tryStoreBiome(x, z, biomeId);
        }
        return getBiomeFromId(biomeId);
    }

    // Read the Biome instance from the Cell
    public Biome readBiome(Cell cell, int x, int z) {
        int biome = getBiomeId(cell, x, z);
        biomeCache.tryStoreBiome(x, z, biome);
        return getBiomeFromId(biome);
    }

    // Compute the biome id at a given block position
    protected int computeBiome(Cell cell, int x, int z, boolean load) {
        getWorldLookup().applyCell(cell, x, z, load);
        return getBiomeId(cell, x, z);
    }

    // Get the Biome instance from the biome id
    private Biome getBiomeFromId(int biome) {
        Biome result = context.biomeContext.biomes.get(biome);
        Preconditions.checkNotNull(result, "NULL BIOME D:");
        return result;
    }

    // Determine the biome id from the cell
    private int getBiomeId(Cell cell, int x, int z) {
        BiomeResources resources = this.getResources();
        int biome = resources.biomemap.provideBiome(cell, context.levels);
        if (resources.modifierManager.hasModifiers(cell, context.levels)) {
            int modified = resources.modifierManager.modify(biome, cell, x, z);
            if (BiomeMap.isValid(modified)) {
                biome = modified;
            }
        }
        return biome;
    }

    public boolean canSpawnAt(Cell cell) {
        return !cell.terrain.isSubmerged();
    }

    private BiomeResources getResources() {
        return resources.get();
    }

    private static <T> Dynamic<T> encode(TFBiomeProvider provider, DynamicOps<T> ops) {
        T seed = Codecs.encodeAndGet(Codec.LONG, provider.seed, ops);
        T context = Codecs.encodeAndGet(TFBiomeContext.CODEC, provider.getContext().biomeContext, ops);
        T settings = Codecs.encodeAndGet(TerraSettings.CODEC, provider.getContext().terraSettings, ops);
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("seed"), seed,
                ops.createString("game_data"), context,
                ops.createString("generator_settings"), settings)
        ));
    }

    private static <T> TFBiomeProvider decode(Dynamic<T> dynamic) {
        long seed = Codecs.decodeAndGet(Codec.LONG, dynamic.get("seed"));
        TFBiomeContext gameContext = Codecs.decodeAndGet(TFBiomeContext.CODEC, dynamic.get("game_data"));
        TerraSettings settings = Codecs.decodeAndGet(TerraSettings.CODEC, dynamic.get("generator_settings"));
        settings.world.seed = seed;
        return new TFBiomeProvider(new TerraContext(settings, gameContext));
    }
}
