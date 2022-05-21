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

import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class NoiseCaveCarver {
    private static final int CHUNK_AREA = 16 * 16;

    public static void carve(ChunkAccess chunk,
                             CarverChunk carver,
                             Generator generator,
                             NoiseCave config,
                             boolean carve) {
        var pos = new BlockPos.MutableBlockPos();

        int minY = generator.getMinY();
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();

        for (int i = 0; i < CHUNK_AREA; i++) {
            int dx = i & 15;
            int dz = i >> 4;
            int x = startX + dx;
            int z = startZ + dz;

            int surface = getSurface(x, z, chunk, generator, carver);
            int y = config.getHeight(x, z);

            float value = carver.modifier.getValue(x, z);
            int cavern = config.getCavernSize(x, z, value);
            if (cavern == 0) continue;

            int floor = config.getFloorDepth(x, z, cavern);
            int top = MathUtil.clamp(y + cavern, minY, surface);
            int bottom = MathUtil.clamp(y - floor, minY, surface);

            if (top - bottom < 2) continue;

            var biome = carver.getBiome(x, z, config, generator);

            if (carve) {
                carve(chunk, biome, dx, dz, bottom, top, surface, pos);
            }
        }
    }

    private static void carve(ChunkAccess chunk, Holder<Biome> biome, int dx, int dz, int bottom, int top, int surface, BlockPos.MutableBlockPos pos) {
        var air = Blocks.AIR.defaultBlockState();

        int biomeX = dx >> 2;
        int biomeZ = dz >> 2;
        int maxBiomeY = (surface - 16) >> 2;
        for (int cy = bottom; cy <= top; cy++) {
            pos.set(dx, cy, dz);

            if (!chunk.getBlockState(pos).getFluidState().isEmpty()) continue;

            chunk.setBlockState(pos, air, false);

            if ((cy >> 2) >= maxBiomeY) continue;

            int biomeY = (cy & 15) >> 2;
            int sectionIndex = chunk.getSectionIndex(cy);
            var section = chunk.getSection(sectionIndex);

            section.getBiomes().getAndSetUnchecked(biomeX, biomeY, biomeZ, biome);
        }
    }

    private static int getSurface(int x, int z, ChunkAccess chunk, Generator generator, CarverChunk carverChunk) {
        float mask = carverChunk.getCarvingMask(x, z);
        int surface = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
        if (surface > generator.getSeaLevel() || surface < generator.getSeaLevel() - 16) {
            surface += 9;
        }
        return surface - NoiseUtil.floor(16 * mask);
    }
}
