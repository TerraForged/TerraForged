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

package com.terraforged.mod.worldgen.biome.surface;

import com.terraforged.mod.worldgen.terrain.TerrainData;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class Surface {
    protected static final Tag<Block> ERODABLE = BlockTags.DIRT;

    public static void apply(TerrainData terrainData, ChunkAccess chunk, ChunkGenerator generator) {
        float norm = 55 * (generator.getGenDepth() / 255F);

        var pos = new BlockPos.MutableBlockPos();
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                int y = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, dx, dz);

                float gradient = terrainData.getGradient(dx, dz, norm);
                if (y < 60 || gradient < 0.6F) continue;

                var solid = findSolid(pos.set(dx, y, dz), chunk);
                if (solid == null) continue;

                int bottom = pos.getY();
                while (y > bottom) {
                    chunk.setBlockState(pos.setY(y), solid, false);
                    y--;
                }
            }
        }
    }

    public static boolean isErodable(BlockState state) {
        return ERODABLE.contains(state.getBlock());
    }

    protected static BlockState findSolid(BlockPos.MutableBlockPos pos, ChunkAccess chunk) {
        var state = chunk.getBlockState(pos);

        if (!isErodable(state)) return null;

        for (int y = pos.getY() - 1, bottom = Math.max(0, pos.getY() - 20); y > bottom; y--) {
            state = chunk.getBlockState(pos.setY(y));

            // Stop when we hit non-erodable material
            if (!isErodable(state)) {
                return state;
            }
        }

        return null;
    }
}
