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

package com.terraforged.mod.chunk.generator;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.mod.biome.TFBiomeContainer;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.fix.ChunkCarverFix;
import com.terraforged.mod.featuremanager.template.StructureUtils;
import com.terraforged.mod.featuremanager.util.identity.FeatureIdentifier;
import com.terraforged.mod.featuremanager.util.identity.IdentityCache;
import com.terraforged.mod.profiler.watchdog.WarnTimer;
import com.terraforged.mod.profiler.watchdog.Watchdog;
import com.terraforged.mod.profiler.watchdog.WatchdogContext;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class TerrainCarver implements Generator.Carvers {

    private static final String TYPE = "Carver";

    private final TFChunkGenerator generator;
    private final long timeout;
    private final IdentityCache<ConfiguredCarver<?>> identityCache = new IdentityCache<>(FeatureIdentifier::getIdentity);

    public TerrainCarver(TFChunkGenerator generator) {
        this.generator = generator;
        this.timeout = Watchdog.getWatchdogHangTime();
    }

    @Override
    public void carveTerrain(BiomeManager biomes, IChunk chunk, GenerationStage.Carving type) {
        try (WatchdogContext context = Watchdog.punchIn(chunk, generator, timeout)) {
            carve(chunk, type, context);
        }
    }

    private void carve(IChunk chunk, GenerationStage.Carving type, WatchdogContext context) {
        boolean nearRiver = nearRiver(chunk.getPos());
        boolean nearStructure = StructureUtils.hasOvergroundStructure(chunk);
        ChunkCarverFix carverChunk = new ChunkCarverFix(chunk, generator.getMaterials(), nearStructure, nearRiver);

        SharedSeedRandom random = new SharedSeedRandom();
        ChunkPos chunkpos = carverChunk.getPos();
        int chunkX = chunkpos.x;
        int chunkZ = chunkpos.z;

        int seaLevel = generator.getSeaLevel();
        TFBiomeContainer biomeContainer = TFBiomeContainer.getOrNull(chunk);
        BiomeLookup lookup = new BiomeLookup(chunkpos, biomeContainer);
        BitSet mask = carverChunk.getCarvingMask(type);
        Biome biome = TerrainCarver.getBiome(biomeContainer, generator.getBiomeSource(), chunkpos);
        BiomeGenerationSettings settings = biome.getGenerationSettings();

        WarnTimer timer = Watchdog.getWarnTimer();

        ListIterator<Supplier<ConfiguredCarver<?>>> iterator = settings.getCarvers(type).listIterator();
        for (int cx = chunkX - 8; cx <= chunkX + 8; ++cx) {
            for (int cz = chunkZ - 8; cz <= chunkZ + 8; ++cz) {
                while (iterator.hasNext()) {
                    int index = iterator.nextIndex();
                    ConfiguredCarver<?> carver = iterator.next().get();
                    random.setLargeFeatureSeed(generator.getSeed() + index, cx, cz);
                    if (carver.isStartChunk(random, cx, cz)) {
                        long timestamp = timer.now();
                        carver.carve(carverChunk, lookup, random, seaLevel, cx, cz, chunkX, chunkZ, mask);
                        Generator.checkTime(TYPE, carver, identityCache, timer, timestamp, context);
                    }
                }

                // rewind
                while (iterator.hasPrevious()) {
                    iterator.previous();
                }
            }
        }
    }

    private class BiomeLookup implements Function<BlockPos, Biome> {

        private final ChunkPos chunkPos;
        private final TFBiomeContainer biomes;
        private final Cell cell = new Cell();

        private BiomeLookup(ChunkPos chunkPos, @Nullable TFBiomeContainer biomes) {
            this.chunkPos = chunkPos;
            this.biomes = biomes;
        }

        @Override
        public Biome apply(BlockPos pos) {
            if (biomes != null && (pos.getX() >> 4) == chunkPos.x && (pos.getZ() >> 4) == chunkPos.z) {
                // Method masks to chunk-local coordinates
                return biomes.getBiome(pos.getX(), pos.getZ());
            }
            return generator.getBiomeSource().lookupBiome(cell, pos.getX(), pos.getZ(), false);
        }
    }

    private boolean nearRiver(ChunkPos pos) {
        try (ChunkReader reader = generator.getChunkReader(pos)) {
            return reader.getCell(8, 8).riverMask < 0.33F;
        }
    }

    private static Biome getBiome(@Nullable TFBiomeContainer container, TFBiomeProvider biomeProvider, ChunkPos pos) {
        if (container == null) {
            try (Resource<Cell> resource = Cell.getResource()) {
                return biomeProvider.lookupBiome(resource.get(), pos.getMinBlockX(), pos.getMinBlockZ(), false);
            }
        }
        return container.getBiome(0, 0);
    }
}
