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

import com.terraforged.api.material.state.States;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.chunk.ChunkReader;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;

public class TerrainHelper {

    private final Module noise;
    private final float radius;

    public TerrainHelper(int seed, float radius) {
        this.noise = Source.perlin(++seed, 8, 1).alpha(0.75);
        this.radius = radius;
    }

    public void flatten(IWorld world, IChunk chunk, ChunkReader reader, int chunkStartX, int chunkStartZ) {
        ObjectList<AbstractVillagePiece> pieces = new ObjectArrayList<>(10);
        collectPieces(world, chunk, pieces);
        buildBases(chunk, reader, pieces, chunkStartX, chunkStartZ);
    }

    // see NoiseChunkGenerator
    private void collectPieces(IWorld world, IChunk chunk, ObjectList<AbstractVillagePiece> pieces) {
        ChunkPos pos = chunk.getPos();
        for (Structure<?> structure : Feature.ILLAGER_STRUCTURES) {
            String name = structure.getStructureName();
            LongIterator structureIds = chunk.getStructureReferences(name).iterator();

            while (structureIds.hasNext()) {
                long id = structureIds.nextLong();
                ChunkPos structurePos = new ChunkPos(id);
                IChunk neighbourChunk = world.getChunk(structurePos.asBlockPos());
                StructureStart structurestart = neighbourChunk.getStructureStart(name);
                if (structurestart != null && structurestart.isValid()) {
                    for (StructurePiece structurepiece : structurestart.getComponents()) {
                        if (structurepiece.func_214810_a(pos, 12) && structurepiece instanceof AbstractVillagePiece) {
                            AbstractVillagePiece piece = (AbstractVillagePiece) structurepiece;
                            JigsawPattern.PlacementBehaviour placement = piece.getJigsawPiece().getPlacementBehaviour();
                            if (placement == JigsawPattern.PlacementBehaviour.RIGID) {
                                pieces.add(piece);
                            }
                        }
                    }
                }
            }
        }
    }

    // lowers or raises the terrain matcher the base height of each structure piece
    private void buildBases(IChunk chunk, ChunkReader reader,ObjectList<AbstractVillagePiece> pieces, int chunkStartX, int chunkStartZ) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        MutableBoundingBox chunkBounds = new MutableBoundingBox(chunkStartX, chunkStartZ, chunkStartX + 15, chunkStartZ + 15);
        for (AbstractVillagePiece piece : pieces) {
            MutableBoundingBox pieceBounds = piece.getBoundingBox();
            int length = Math.min(pieceBounds.maxX - pieceBounds.minX, pieceBounds.maxZ - pieceBounds.minZ);
            int borderRadius = Math.max(5, NoiseUtil.round(length * radius));
            MutableBoundingBox expanded = expand(pieceBounds, borderRadius);

            if (!expanded.intersectsWith(chunkBounds)) {
                continue;
            }

            // intersecting area between the generator bounds and the village piece bounds
            int startX = Math.max(chunkStartX, expanded.minX);
            int startZ = Math.max(chunkStartZ, expanded.minZ);
            int endX = Math.min(chunkStartX + 15, expanded.maxX);
            int endZ = Math.min(chunkStartZ + 15, expanded.maxZ);

            // y position of the structure piece
            int level = pieceBounds.minY + piece.getGroundLevelDelta();

            // iterate the intersecting area
            for (int z = startZ; z <= endZ; z++) {
                for (int x = startX; x <= endX; x++) {
                    // local generator coords
                    int dx = x & 15;
                    int dz = z & 15;

                    int surface = chunk.getTopBlockY(Heightmap.Type.OCEAN_FLOOR_WG, dx, dz);
                    if (surface == level) {
                        continue;
                    }

                    if (surface > level) {
                        flatten(chunk, reader, pieceBounds, pos.setPos(x, surface, z), dx, dz, level, surface, borderRadius);
                    } else {
                        // piece is higher than world-surface .: raise ground to form a base
                        raise(chunk, pieceBounds, pos.setPos(x, surface, z), dx, dz, level, surface, borderRadius);
                    }
                }
            }
        }
    }

    private void flatten(IChunk chunk, ChunkReader reader, MutableBoundingBox bounds, BlockPos.Mutable pos, int dx, int dz, int level, int surface, int borderRadius) {
        if (pos.getX() >= bounds.minX && pos.getX() <= bounds.maxX && pos.getZ() >= bounds.minZ && pos.getZ() <= bounds.maxZ) {
            Cell<?> cell = reader.getCell(dx, dz);
            cell.steepness = 0F;
            cell.sediment = 0F;
            cell.erosion = 0F;
            for (int dy = level + 1; dy <= surface; dy++) {
                chunk.setBlockState(pos.setPos(dx, dy, dz), Blocks.AIR.getDefaultState(), false);
            }
        }
    }

    private void raise(IChunk chunk, MutableBoundingBox bounds, BlockPos.Mutable pos, int dx, int dz, int level, int surface, int borderRadius) {
        float radius2 = Math.max(1, borderRadius * borderRadius * noise.getValue(pos.getX(), pos.getZ()));
        float alpha = getAlpha(bounds, radius2, pos.getX(), pos.getZ());
        if (alpha == 0F) {
            // outside of the raise-able radius
            return;
        }

        int heightDelta = level - surface - 1;
        if (alpha < 1F) {
            // sharper fall-off
            alpha = alpha * alpha;
            heightDelta = NoiseUtil.round(alpha * heightDelta);
        }

        BlockState state = States.STONE.getDefaultState();
        for (int dy = surface + heightDelta; dy >= surface; dy--) {
            pos.setPos(dx, dy, dz);
            if (chunk.getBlockState(pos).isSolid()) {
                return;
            }
            chunk.setBlockState(pos.setPos(dx, dy, dz), state, false);
        }
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

    private static float getAlpha(MutableBoundingBox box, float radius2, int x, int y) {
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
