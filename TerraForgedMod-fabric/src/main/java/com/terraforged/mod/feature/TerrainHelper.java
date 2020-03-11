/*
 *
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

package com.terraforged.mod.feature;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.StructureFeature;

public class TerrainHelper {

    private final Module noise;
    private final float radius;

    public TerrainHelper(int seed, float radius) {
        this.noise = Source.perlin(++seed, 8, 1).alpha(0.75);
        this.radius = radius;
    }

    public void flatten(IWorld world, Chunk chunk, int chunkStartX, int chunkStartZ) {
        ObjectList<PoolStructurePiece> pieces = new ObjectArrayList<>(10);
        collectPieces(world, chunk, pieces);
        buildBases(chunk, pieces, chunkStartX, chunkStartZ);
    }

    // see NoiseChunkGenerator
    private void collectPieces(IWorld world, Chunk chunk, ObjectList<PoolStructurePiece> pieces) {
        ChunkPos pos = chunk.getPos();
        for (StructureFeature<?> structure : Feature.JIGSAW_STRUCTURES) {
            String name = structure.getName();
            LongIterator structureIds = chunk.getStructureReferences(name).iterator();

            while (structureIds.hasNext()) {
                long id = structureIds.nextLong();
                ChunkPos structurePos = new ChunkPos(id);
                Chunk neighbourChunk = world.getChunk(structurePos.getCenterBlockPos());
                StructureStart structurestart = neighbourChunk.getStructureStart(name);
                if (structurestart != null && structurestart.isInExistingChunk()) {
                    for (StructurePiece structurepiece : structurestart.getChildren()) {
                        if (structurepiece.method_16654(pos, 12) && structurepiece instanceof PoolStructurePiece) {
                            PoolStructurePiece piece = (PoolStructurePiece) structurepiece;
                            StructurePool.Projection placement = piece.getPoolElement().getProjection();
                            if (placement == StructurePool.Projection.RIGID) {
                                pieces.add(piece);
                            }
                        }
                    }
                }
            }
        }
    }

    // try to fill in type air beneath village pieces with the biomes default filler block
    private void buildBases(Chunk chunk, ObjectList<PoolStructurePiece> pieces, int chunkStartX, int chunkStartZ) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        BlockBox chunkBounds = new BlockBox(chunkStartX, chunkStartZ, chunkStartX + 15, chunkStartZ + 15);
        for (PoolStructurePiece piece : pieces) {
            BlockBox pieceBounds = piece.getBoundingBox();

            int length = Math.min(pieceBounds.maxX - pieceBounds.minX, pieceBounds.maxZ - pieceBounds.minZ);
            int borderRadius = Math.max(5, NoiseUtil.round(length * radius));
            BlockBox expanded = expand(pieceBounds, borderRadius);

            if (!expanded.intersects(chunkBounds)) {
                continue;
            }

            // intersecting area between the generator bounds and the village piece bounds
            int startX = Math.max(chunkStartX, expanded.minX);
            int startZ = Math.max(chunkStartZ, expanded.minZ);
            int endX = Math.min(chunkStartX + 15, expanded.maxX);
            int endZ = Math.min(chunkStartZ + 15, expanded.maxZ);

            // iterate the intersecting area
            for (int z = startZ; z <= endZ; z++) {
                for (int x = startX; x <= endX; x++) {
                    // local generator coords
                    int dx = x & 15;
                    int dz = z & 15;

                    // the paste position of the village piece
                    BlockPos position = piece.getPos();

                    int offset = piece.getGroundLevelDelta();
                    int level = position.getY() + (offset - 1);
                    int surface = chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, dx, dz) - 1;
                    int height = level - surface;
                    if (height <= 0) {
                        continue;
                    }

                    float radius2 = Math.max(1, borderRadius * borderRadius * noise.getValue(x, z));
                    float alpha = getAlpha(pieceBounds, radius2, x, z);
                    if (alpha == 0F) {
                        continue;
                    }

                    if (alpha < 1F) {
                        alpha = alpha * alpha;
                        height = NoiseUtil.round(alpha * height);
                    }

                    BlockState state = Blocks.STONE.getDefaultState();
                    for (int dy = surface + height; dy >= surface; dy--) {
                        pos.set(dx, dy, dz);
                        if (chunk.getBlockState(pos).isOpaque()) {
                            break;
                        }
                        chunk.setBlockState(pos.set(dx, dy, dz), state, false);
                    }
                }
            }
        }
    }

    private static BlockBox expand(BlockBox box, int radius) {
        return new BlockBox(
                box.minX - radius,
                box.minY,
                box.minZ - radius,
                box.maxX + radius,
                box.maxY,
                box.maxZ + radius
        );
    }

    private static float getAlpha(BlockBox box, float radius2, int x, int y) {
        int dx = x < box.minX ? box.minX - x : x > box.maxX ? x - box.maxX : 0;
        int dy = y < box.minZ ? box.minZ - y : y > box.maxZ ? y - box.maxZ : 0;
        int d2 = dx * dx + dy * dy;
        if (d2 == 0) {
            return 1F;
        }
        if (d2 > radius2) {
            return 0F;
        }
        return 1 - (d2 / radius2);
    }
}
