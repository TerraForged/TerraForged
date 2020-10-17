/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.mod.chunk.util;

import com.terraforged.api.chunk.ChunkDelegate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.palette.PalettedContainer;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import org.apache.commons.lang3.Validate;

/**
 * A ChunkPrimer wrapper that handles setting BlockStates within the chunk & updating heightmaps accordingly
 */
public class FastChunk extends ChunkDelegate {

    private static final int arraySize = 256;
    private static final int bitsPerEntry = 9;
    private static final long maxEntryValue = (1L << bitsPerEntry) - 1L;

    private final int blockX;
    private final int blockZ;
    private final ChunkPrimer primer;
    private final Heightmap worldSurface;
    private final Heightmap oceanSurface;
    private final BlockPos.Mutable mutable = new BlockPos.Mutable();

    protected FastChunk(ChunkPrimer primer) {
        super(primer);
        this.primer = primer;
        this.blockX = primer.getPos().getXStart();
        this.blockZ = primer.getPos().getZStart();
        this.worldSurface = primer.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        this.oceanSurface = primer.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
    }

    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean falling) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            ChunkSection section = primer.getSection(pos.getY() >> 4);
            section.lock();
            int dx = pos.getX() & 15;
            int dy = pos.getY() & 15;
            int dz = pos.getZ() & 15;
            BlockState replaced = section.setBlockState(dx, dy, dz, state, false);
            if (state.getBlock() != Blocks.AIR) {
                mutable.setPos(blockX + dx, pos.getY(), blockZ + dz);
                if (state.getLightValue(primer, mutable) != 0) {
                    primer.addLightPosition(mutable);
                }
                worldSurface.update(dx, pos.getY(), dz, state);
                oceanSurface.update(dx, pos.getY(), dz, state);
            }
            section.unlock();
            return replaced;
        }
        return Blocks.VOID_AIR.getDefaultState();
    }

    public void setBiomes(BiomeContainer biomes) {
        primer.setBiomes(biomes);
    }

    public static IChunk wrap(IChunk chunk) {
        if (chunk instanceof FastChunk) {
            return chunk;
        }
        if (chunk.getClass() == ChunkPrimer.class) {
            return new FastChunk((ChunkPrimer) chunk);
        }
        return chunk;
    }

    public static void updateWGHeightmaps(IChunk chunk, BlockPos.Mutable pos) {
        int topY = chunk.getTopFilledSegment() + 15;
        long[] ocean = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG).getDataArray();
        long[] surface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG).getDataArray();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                boolean flag = false;
                for (int y = topY; y >= 0; y--) {
                    BlockState state = chunk.getBlockState(pos.setPos(x, y, z));
                    if (state.getBlock() != Blocks.AIR) {
                        if (!flag) {
                            // WORLD_SURFACE_WG predicate is when block != air .: can set the height at this y value
                            setAt(surface, index(x, z), y + 1);
                            // flag = true means subsequent y values are ignored
                            flag = true;
                        }
                        if (Heightmap.Type.OCEAN_FLOOR_WG.getHeightLimitPredicate().test(state)) {
                            setAt(ocean, index(x, z), y + 1);
                            // no more heightmaps to update at this x/z so can exit the y loop here
                            break;
                        }
                    }
                }
            }
        }
    }

    // from BitArray
    private static void setAt(long[] longArray, int index, int value) {
        Validate.inclusiveBetween(0L, (arraySize - 1), index);
        Validate.inclusiveBetween(0L, maxEntryValue, value);
        int i = index * bitsPerEntry;
        int j = i >> 6;
        int k = (index + 1) * bitsPerEntry - 1 >> 6;
        int l = i ^ j << 6;
        longArray[j] = longArray[j] & ~(maxEntryValue << l) | ((long) value & maxEntryValue) << l;
        if (j != k) {
            int i1 = 64 - l;
            int j1 = bitsPerEntry - i1;
            longArray[k] = longArray[k] >>> j1 << j1 | ((long) value & maxEntryValue) >> i1;
        }
    }

    private static int index(int x, int z) {
        return x + (z << 4);
    }
}
