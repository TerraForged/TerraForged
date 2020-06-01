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

package com.terraforged.feature;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import me.dags.noise.util.NoiseUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.structure.*;

import java.util.List;
import java.util.Set;

public class TerrainHelper {

    private static final Set<IStructurePieceType> SURFACE_STRUCTURES = ImmutableSet.of(
            IStructurePieceType.PCP, // outpost
            IStructurePieceType.NVI, // village
            IStructurePieceType.TEJP, // jungle temple
            IStructurePieceType.IGLU, // igloo
            IStructurePieceType.TEDP // desert pyramid
    );

    private final float radius;

    public TerrainHelper(float radius) {
        this.radius = radius;
    }

    public void flatten(IWorld world, IChunk chunk) {
        ObjectList<StructurePiece> pieces = new ObjectArrayList<>(10);
        collectPieces(world, chunk, pieces);
        buildBases(chunk, pieces);
    }

    // see NoiseChunkGenerator
    private void collectPieces(IWorld world, IChunk chunk, ObjectList<StructurePiece> pieces) {
        ChunkPos pos = chunk.getPos();
        for (Structure<?> structure : Structure.ILLAGER_STRUCTURES) {
            String name = structure.getStructureName();
            LongIterator structureIds = chunk.getStructureReferences(name).iterator();

            while (structureIds.hasNext()) {
                long id = structureIds.nextLong();
                ChunkPos structurePos = new ChunkPos(id);
                IChunk neighbourChunk = world.getChunk(structurePos.asBlockPos());
                StructureStart structurestart = neighbourChunk.getStructureStart(name);
                if (structurestart != null && structurestart.isValid()) {
                    for (StructurePiece structurepiece : structurestart.getComponents()) {
                        // collect if piece is within radius of the chunk
                        if (structurepiece.func_214810_a(pos, 12)) {
                            collectPiece(structurepiece, pieces);
                        }
                    }
                }
            }
        }
    }

    // lowers or raises the terrain matcher the base height of each structure piece
    private void buildBases(IChunk chunk, ObjectList<StructurePiece> pieces) {
        int chunkStartX = chunk.getPos().getXStart();
        int chunkStartZ = chunk.getPos().getZStart();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        ObjectListIterator<StructurePiece> iterator = pieces.iterator();
        MutableBoundingBox chunkBounds = new MutableBoundingBox(chunkStartX, chunkStartZ, chunkStartX + 15, chunkStartZ + 15);

        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                int x = chunkStartX + dx;
                int z = chunkStartZ + dz;
                int surface = chunk.getTopBlockY(Heightmap.Type.OCEAN_FLOOR_WG, dx, dz);
                float y = surface;

                int highestOffset = 0;
                StructurePiece highest = null;
                while (iterator.hasNext()) {
                    StructurePiece piece = iterator.next();
                    MutableBoundingBox pieceBounds = piece.getBoundingBox();
                    int length = Math.min(pieceBounds.maxX - pieceBounds.minX, pieceBounds.maxZ - pieceBounds.minZ);
                    int borderRadius = Math.min(5, Math.max(10, NoiseUtil.round(length * radius)));
                    MutableBoundingBox expanded = expand(pieceBounds, borderRadius);
                    if (!expanded.intersectsWith(chunkBounds)) {
                        continue;
                    }

                    int offset = getGroundLevelDelta(piece);
                    int level = pieceBounds.minY + offset;
                    if (level > y) {
                        y = raise(pieceBounds, pos.setPos(x, surface, z), level, y, borderRadius);
                    }

                    if (x > pieceBounds.minX && x < pieceBounds.maxX && z > pieceBounds.minZ && z < pieceBounds.maxZ) {
                        if (highest == null || pieceBounds.minY > highest.getBoundingBox().minY) {
                            highest = piece;
                            highestOffset = offset;
                        }
                    }
                }

                // reset iterator for next pass
                iterator.back(pieces.size());

                if (y > surface) {
                    int delta = (int) y - surface;
                    for (int dy = 0; dy < delta; dy++) {
                        pos.setPos(dx, surface + dy, dz);
                        chunk.setBlockState(pos, Blocks.STONE.getDefaultState(), false);
                    }
                }

                if (highest != null) {
                    MutableBoundingBox bounds = highest.getBoundingBox();
                    for (int dy = bounds.minY + highestOffset; dy <= surface; dy++) {
                        pos.setPos(dx, dy, dz);
                        chunk.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
                    }
                }
            }
        }
    }

    private float raise(MutableBoundingBox bounds, BlockPos.Mutable pos, float level, float surface, int borderRadius) {
        float radius2 = Math.max(1, borderRadius * borderRadius);
        float alpha = 1 - getDistAlpha(pos.getX(), pos.getZ(), bounds, radius2);
        alpha = (float) Math.pow(alpha, 2F - alpha);
        return NoiseUtil.lerp(surface, level, alpha);
    }

    private static void collectPiece(StructurePiece structurepiece, List<StructurePiece> list) {
        if (structurepiece instanceof AbstractVillagePiece) {
            AbstractVillagePiece piece = (AbstractVillagePiece) structurepiece;
            JigsawPattern.PlacementBehaviour placement = piece.getJigsawPiece().getPlacementBehaviour();
            if (placement == JigsawPattern.PlacementBehaviour.RIGID) {
                list.add(piece);
            }
        } else if (SURFACE_STRUCTURES.contains(structurepiece.getStructurePieceType())) {
            list.add(structurepiece);
        }
    }

    private static int getGroundLevelDelta(StructurePiece piece) {
        if (piece instanceof AbstractVillagePiece) {
            return ((AbstractVillagePiece) piece).getGroundLevelDelta();
        }
        return 0;
    }

    private static MutableBoundingBox expand(MutableBoundingBox box, int radius) {
        return new MutableBoundingBox(
                box.minX - radius,
                box.minY,
                box.minZ - radius,
                box.maxX + radius,
                box.maxY,
                box.maxZ + radius
        );
    }

    private static float getDistAlpha(int x, int z, MutableBoundingBox box, float radius2) {
        int dx = x < box.minX ? box.minX - x : x > box.maxX ? x - box.maxX : 0;
        int dz = z < box.minZ ? box.minZ - z : z > box.maxZ ? z - box.maxZ : 0;
        int d2 = dx * dx + dz * dz;
        if (d2 == 0) {
            return 0;
        }
        if (d2 > radius2) {
            return 1F;
        }
        return d2 / radius2;
    }
}
