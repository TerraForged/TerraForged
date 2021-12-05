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

package com.terraforged.mod.worldgen.terrain;

import com.terraforged.noise.util.NoiseUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

public class StructureTerrain {
    private static final int MIN_RADIUS = 4;
    private static final int MAX_RADIUS = 10;
    private static final int RADIUS = 20;

    private final ObjectList<StructurePiece> rigids = new ObjectArrayList<>(10);
    private final ObjectList<JigsawJunction> junctions = new ObjectArrayList<>(32);
    protected final ObjectListIterator<StructurePiece> pieceIterator;
    protected final ObjectListIterator<JigsawJunction> junctionIterator;

    protected final BlockState air = Blocks.AIR.defaultBlockState();
    protected final BlockState solid = Blocks.STONE.defaultBlockState();
    protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public StructureTerrain(ChunkAccess chunk, StructureFeatureManager manager) {
        var chunkPos = chunk.getPos();
        var sectionPos = SectionPos.bottomOf(chunk);
        int x = chunkPos.getMinBlockX();
        int z = chunkPos.getMinBlockZ();

        for (var feature : StructureFeature.NOISE_AFFECTING_FEATURES) {
            manager.startsForFeature(sectionPos, feature).forEach(start -> {
                for (var piece : start.getPieces()) {
                    if (!piece.isCloseToChunk(chunkPos, RADIUS)) continue;
                    if (piece.getNoiseEffect() != NoiseEffect.BEARD) continue;

                    if (piece instanceof PoolElementStructurePiece element) {
                        var projection = element.getElement().getProjection();
                        if (projection == StructureTemplatePool.Projection.RIGID) {
                            this.rigids.add(element);
                        }

                        for (var junction : element.getJunctions()) {
                            int sx = junction.getSourceX();
                            int sz = junction.getSourceZ();
                            if (sx > x - RADIUS && sz > z - RADIUS && sx < x + 15 + RADIUS && sz < z + 15 + RADIUS) {
                                this.junctions.add(junction);
                            }
                        }
                    } else {
                        this.rigids.add(piece);
                    }
                }
            });
        }

        this.pieceIterator = rigids.iterator();
        this.junctionIterator = junctions.iterator();
    }

    public void modify(int x, int z, ChunkAccess chunk, TerrainData terrainData) {
        int y = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);

        float maxY = y;
        int maxPosY = y;
        StructurePiece highest = null;

        while (pieceIterator.hasNext()) {
            var piece = pieceIterator.next();
            var bounds = piece.getBoundingBox();
            int length = Math.max(bounds.getXSpan(), bounds.getZSpan());
            float radius = Math.max(4, RADIUS - length);

            int posY = getPieceY(piece);
            maxPosY = Math.max(maxPosY, posY);

            if (highest == null && posY > y) {
                maxY = raise(x, z, bounds, posY, maxY, radius);
            }

            if (x >= bounds.minX() && x <= bounds.maxX() && z >= bounds.minZ() && z <= bounds.maxZ()) {
                if (highest == null || bounds.minY() > highest.getBoundingBox().minY()) {
                    highest = piece;
                }
            }
        }

        boolean raised = raiseTerrain(x, y, z, maxY, chunk, terrainData);
        boolean carved = carveTerrain(x, maxPosY, z, chunk, highest);

        if (raised || carved) {
            terrainData.getHeight().set(x, z, chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z));
        }

        reset();
    }

    protected boolean raiseTerrain(int x, int y, int z, float maxY, ChunkAccess chunk, TerrainData terrainData) {
        int max = (int) maxY;
        if (y + 1 >= max) return false;

        for (int py = y; py < max; py++) {
            chunk.setBlockState(pos.set(x, py, z), solid, false);
        }

        return true;
    }

    protected boolean carveTerrain(int x, int y, int z, ChunkAccess chunk, StructurePiece piece) {
        if (piece == null) return false;

        int surface = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        int ceiling = Math.max(y, surface);

        var bounds = piece.getBoundingBox();
        int minY = getPieceY(piece);
        int maxY = bounds.maxY();

        if (ceiling > maxY + 5) {
            float alpha = getEllipseDistAlpha(x, z, bounds);
            float depth = (ceiling - maxY) * 0.5F;
            maxY += NoiseUtil.round(depth * alpha);
        }

        for (int py = minY; py <= maxY; py++) {
            chunk.setBlockState(pos.set(x, py, z), air, false);
        }

        return true;
    }

    protected void reset() {
        pieceIterator.back(rigids.size());
        junctionIterator.back(rigids.size());
    }

    private static float raise(int x, int z, BoundingBox bounds, float level, float surface, float borderRadius) {
        float radius2 = Math.max(1, borderRadius * borderRadius);
        float distAlpha = 1 - getDistAlpha(x, z, bounds, radius2);
        float alpha = NoiseUtil.pow(distAlpha, 2F - distAlpha);
        return NoiseUtil.lerp(surface, level, alpha);
    }

    protected static float getEllipseDistAlpha(int x, int z, BoundingBox bounds) {
        float radiusX = bounds.getXSpan() * 0.5F;
        float radiusZ = bounds.getZSpan() * 0.5F;
        float centerX = (bounds.minX() + bounds.maxX()) * 0.5F;
        float centerZ = (bounds.minZ() + bounds.maxZ()) * 0.5F;
        float dx = x - centerX;
        float dz = z - centerZ;
        float qx = (dx * dx) / (radiusX * radiusX);
        float qz = (dz * dz) / (radiusZ * radiusZ);
        return NoiseUtil.clamp(1 - qx - qz, 0, 1);
    }

    protected static float getDistAlpha(int x, int z, BoundingBox box, float radius2) {
        int dx = getDist(x, box.minX(), box.maxX());
        int dz = getDist(z, box.minZ(), box.maxZ());
        return getDistAlpha(dx, dz, radius2);
    }

    protected static float getDistAlpha(int dx, int dz, float radius2) {
        int d2 = dx * dx + dz * dz;
        if (d2 == 0)  return 0F;
        if (d2 >= radius2) return 1F;
        return NoiseUtil.sqrt(d2 / radius2);
    }

    protected static int getOffset(StructurePiece piece) {
        if (piece instanceof PoolElementStructurePiece element) {
            return element.getGroundLevelDelta();
        }
        return 0;
    }

    protected static int getPieceY(StructurePiece piece) {
        int y = piece.getBoundingBox().minY();
        if (piece instanceof PoolElementStructurePiece element) {
            y += element.getGroundLevelDelta();
        }
        return y;
    }

    protected static int getDist(int pos, int min, int max) {
        return Math.max(0, Math.max(min - pos, pos - max));
    }

    protected static class Backup extends StructureTerrain {
        public Backup(ChunkAccess chunk, StructureFeatureManager manager) {
            super(chunk, manager);
        }

        public void modify(int x, int z, TerrainData terrain) {
            float height = terrain.getHeight().get(x, z);
            float raised = height;
            float lowered = height;

            while (pieceIterator.hasNext()) {
                var piece = pieceIterator.next();
                if (piece.getNoiseEffect() != NoiseEffect.BEARD) continue;

                float contribution = getPieceHeight(x, z, height, piece);
                raised = Math.max(raised, contribution);
                lowered = Math.min(lowered, contribution);
            }

            while (junctionIterator.hasNext()) {
                var junction = junctionIterator.next();
                float contribution = getJunctionHeight(x, z, height, junction);
                raised = Math.max(raised, contribution);
            }

            if (lowered < height) {
                terrain.getHeight().set(x, z, lowered);
            } else if (raised > height) {
                terrain.getHeight().set(x, z, raised);
            }

            reset();
        }

        protected static float getPieceHeight(int x, int z, float height, StructurePiece piece) {
            var bounds = piece.getBoundingBox();
            int pieceY = getPieceY(piece);

            int length = Math.max(bounds.getXSpan(), bounds.getZSpan());
            float radius = Math.max(4, 24 - length);

            float alpha = getDistAlpha(x, z, bounds, radius * radius);

            if (pieceY > height) {
                return NoiseUtil.lerp(pieceY, height, alpha);
            } else if (pieceY < height && alpha == 0) {
                return pieceY;
            } else {
                return height;
            }
        }

        protected static float getJunctionHeight(int x, int z, float height, JigsawJunction junction) {
            int pieceY = junction.getSourceGroundY() - 1;
            float alpha = getDistAlpha(x, z, junction.getSourceX(), junction.getSourceZ(), MIN_RADIUS);
            if (pieceY > height) {
                float fade = 1 - alpha;
                fade *= fade;
                fade = 1 - fade;
                return NoiseUtil.lerp(pieceY, height, fade);
            }
            return height;
        }

        protected static float getDistAlpha(int x, int z, int px, int pz, float radius2) {
            int dx = x - px;
            int dz = z - pz;
            return getDistAlpha(dx, dz, radius2);
        }
    }
}
