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

import com.google.common.collect.ImmutableSet;
import com.terraforged.engine.concurrent.task.LazySupplier;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.settings.StructureSettings;
import com.terraforged.noise.util.Vec2i;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.Structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class StrongholdGenerator implements Generator.Strongholds {

    private static final ChunkPos[] EMPTY_ARRAY = new ChunkPos[0];

    private final long seed;
    private final int salt;
    private final int count;
    private final int spread;
    private final int distance;
    private final boolean constrain;
    private final Vec2i origin;
    private final TFBiomeProvider biomeProvider;
    private final LazySupplier<ChunkPos[]> positions;
    private final LazySupplier<Set<ChunkPos>> lookup;

    public StrongholdGenerator(long seed, TFBiomeProvider biomeProvider) {
        this.seed = seed;
        // TODO: @Breaking: Center strongholds on the origin of the continent when set to continent center.
        //  This requires the origin to be stored on the GeneratorContext since the world spawn can changed
        //  after world creation.
        this.origin = new Vec2i(0, 0);
        this.biomeProvider = biomeProvider;
        this.positions = LazySupplier.of(this::generate);
        this.lookup = positions.then(ImmutableSet::copyOf);
        this.salt = getStrongholdSetting(biomeProvider, s -> s.salt);
        this.count = getStrongholdSetting(biomeProvider, s -> s.count);
        this.spread = getStrongholdSetting(biomeProvider, s -> s.spread);
        this.distance = getStrongholdSetting(biomeProvider, s -> s.distance);
        this.constrain = biomeProvider.getContext().terraSettings.structures.stronghold.constrainToBiomes;
    }

    @Override
    public boolean isStrongholdChunk(ChunkPos pos) {
        return lookup.get().contains(pos);
    }

    @Override
    public BlockPos findNearestStronghold(BlockPos pos) {
        double distance2 = Double.MAX_VALUE;
        BlockPos.Mutable nearest = null;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        final ChunkPos[] chunks = positions.get();
        for(ChunkPos chunkpos : chunks) {
            mutable.set((chunkpos.x << 4) + 8, 32, (chunkpos.z << 4) + 8);
            double dist2 = mutable.distSqr(pos);

            if (nearest == null) {
                nearest = new BlockPos.Mutable();
                nearest.set(mutable);
                distance2 = dist2;
            } else if (dist2 < distance2) {
                nearest.set(mutable);
                distance2 = dist2;
            }
        }

        if (nearest != null) {
            return nearest.immutable();
        }

        return null;
    }

    private ChunkPos[] generate() {
        if (count > 0 && spread > 0 && distance > 0) {
            final int originX = origin.x;
            final int originZ = origin.y;

            int count = this.count;
            int spread = this.spread;
            int distance = this.distance;
            TFBiomeProvider biomeProvider = this.biomeProvider;
            Predicate<Biome> biomePredicate = getStrongholdBiomes(biomeProvider);

            int chunkX = 0;
            int chunkZ = 0;

            // TODO: @Breaking: Combine salt with seed
            final Random random = new Random(salt);
            double angle = random.nextDouble() * Math.PI * 2.0D;

            List<ChunkPos> chunks = new ArrayList<>();
            for(int i = 0; i < count; ++i) {
                double pos = (4 * distance + distance * chunkZ * 6) + (random.nextDouble() - 0.5D) * distance * 2.5D;
                int x = originX + (int) Math.round(Math.cos(angle) * pos);
                int z = originZ + (int) Math.round(Math.sin(angle) * pos);

                int chunkCenterX = (x << 4) + 8;
                int chunkCenterZ = (z << 4) + 8;
                BlockPos blockpos = biomeProvider.findBiomeHorizontal(chunkCenterX, 0, chunkCenterZ, 112, biomePredicate, random);

                if (blockpos != null) {
                    x = blockpos.getX() >> 4;
                    z = blockpos.getZ() >> 4;
                }

                // Record the position if it is a valid stronghold biome or if biome constraining is disabled.
                if (blockpos != null || !constrain) {
                    chunks.add(new ChunkPos(x, z));
                }

                angle += (Math.PI * 2.0D) / spread;
                ++chunkX;

                if (chunkX == spread) {
                    ++chunkZ;
                    chunkX = 0;
                    spread = spread + 2 * spread / (chunkZ + 1);
                    spread = Math.min(spread, count - i);
                    angle += random.nextDouble() * Math.PI * 2.0D;
                }
            }
            return chunks.toArray(EMPTY_ARRAY);
        }

        return EMPTY_ARRAY;
    }

    private static Predicate<Biome> getStrongholdBiomes(TFBiomeProvider biomeProvider) {
        Set<Biome> biomes = new HashSet<>();
        for(Biome biome : biomeProvider.possibleBiomes()) {
            if (biome.getGenerationSettings().isValidStart(Structure.STRONGHOLD)) {
                biomes.add(biome);
            }
        }
        return biomes::contains;
    }

    private static int getStrongholdSetting(TFBiomeProvider biomeProvider, ToIntFunction<StructureSettings.StructureSpread> func) {
        StructureSettings.StructureSpread settings = biomeProvider.getSettings().structures.stronghold;
        if (settings.disabled) {
            return -1;
        }
        return func.applyAsInt(settings);
    }
}
