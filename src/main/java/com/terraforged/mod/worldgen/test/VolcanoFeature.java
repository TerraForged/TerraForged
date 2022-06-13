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

package com.terraforged.mod.worldgen.test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class VolcanoFeature extends Feature<VolcanoConfig> {
    protected final ThreadLocal<Volcano.Cache> localCache = ThreadLocal.withInitial(Volcano.Cache::new);

    public VolcanoFeature() {
        super(VolcanoConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<VolcanoConfig> context) {
        if (true) return true;

        var seed = context.level().getSeed();
        int chunkX = context.origin().getX() >> 4;
        int chunkZ = context.origin().getZ() >> 4;
        var cache = localCache.get().reset();

        Volcano.collectPoints(seed, chunkX, chunkZ, context, context.config(), cache, VolcanoFeature::test);

        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        var chunk = context.level().getChunk(chunkX, chunkZ);

        for (int dz = 0; dz < 16; ++dz) {
            for (int dx = 0; dx < 16; ++dx) {
                int x = minX + dx;
                int z = minZ + dz;

                var value = Volcano.getHighest(x, z, context.config(), cache);
                if (value.height == 0) continue;

                int height = value.height;
                int surface = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, dx, dz) - 1;
                var filler = Blocks.STONE.defaultBlockState();

                if (value.mouth && height < surface) {
                    height = surface + 5;
                    surface = value.height;
                    filler = Blocks.AIR.defaultBlockState();
                }

                fillColumn(dx, dz, height, surface, value, context.config(), chunk, filler);
            }
        }

        return true;
    }

    private static void fillColumn(int x, int z,
                                     int height,
                                     int surface,
                                     Volcano.Value value,
                                     VolcanoConfig config,
                                     ChunkAccess chunk,
                                     BlockState filler) {

        var lava = getFluid(value.hash);
        int fluidLevel = getFluidLevel(value.hash, config);

        for (int y = height; y > surface; --y) {
            int index = chunk.getSectionIndex(y);
            var section = chunk.getSection(index);

            var block = filler.isAir() && y <= fluidLevel ? lava : filler;

            section.setBlockState(x, y & 15, z, block, false);
        }
    }

    private static int getFluidLevel(long hash, VolcanoConfig config) {
        double height = config.fluidLevel().get(Volcano.Noise.rand(hash, Volcano.Noise.HEIGHT_2));

        return Volcano.toHeightValue(height);
    }

    private static BlockState getFluid(long hash) {
        double noise = Volcano.Noise.rand(hash, Volcano.Noise.FLUID_FILLER);
        return noise < 0.5 ? Blocks.WATER.defaultBlockState() : Blocks.LAVA.defaultBlockState();
    }

    private static boolean test(int x, int z, FeaturePlaceContext<VolcanoConfig> context) {
        int y = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.level(), context.level().getLevel().getChunkSource().randomState());

        if (true) return y < 180;

        var biome = context.level().getBiome(new BlockPos(x, y, z));

        return context.config().validBiome(biome);
    }
}
