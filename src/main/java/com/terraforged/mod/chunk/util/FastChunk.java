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

package com.terraforged.mod.chunk.util;

import com.google.common.collect.ImmutableSet;
import com.terraforged.mod.api.chunk.ChunkDelegate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;

import java.util.Set;

/**
 * A ChunkPrimer wrapper that handles setting BlockStates within the chunk & updating heightmaps accordingly
 */
public class FastChunk extends ChunkDelegate {

    private static final Set<Heightmap.Type> HEIGHT_MAPS = ImmutableSet.of(Heightmap.Type.OCEAN_FLOOR_WG, Heightmap.Type.WORLD_SURFACE_WG);

    private final int blockX;
    private final int blockZ;
    private final ChunkPrimer primer;
    private final Heightmap worldSurface;
    private final Heightmap oceanSurface;
    private final BlockPos.Mutable mutable = new BlockPos.Mutable();

    protected FastChunk(ChunkPrimer primer) {
        super(primer);
        this.primer = primer;
        this.blockX = primer.getPos().getMinBlockX();
        this.blockZ = primer.getPos().getMinBlockZ();
        this.worldSurface = primer.getOrCreateHeightmapUnprimed(Heightmap.Type.WORLD_SURFACE_WG);
        this.oceanSurface = primer.getOrCreateHeightmapUnprimed(Heightmap.Type.OCEAN_FLOOR_WG);
    }

    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean falling) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            ChunkSection section = primer.getOrCreateSection(pos.getY() >> 4);
            section.acquire();
            int dx = pos.getX() & 15;
            int dy = pos.getY() & 15;
            int dz = pos.getZ() & 15;
            BlockState replaced = section.setBlockState(dx, dy, dz, state, false);
            if (!state.isAir()) {
                mutable.set(blockX + dx, pos.getY(), blockZ + dz);
                if (state.getLightValue(primer, mutable) != 0) {
                    primer.addLight(mutable);
                }
                worldSurface.update(dx, pos.getY(), dz, state);
                oceanSurface.update(dx, pos.getY(), dz, state);
            }
            section.release();
            return replaced;
        }
        return Blocks.VOID_AIR.defaultBlockState();
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

    public static void updateWGHeightmaps(IChunk chunk) {
        Heightmap.primeHeightmaps(chunk, HEIGHT_MAPS);
    }
}
