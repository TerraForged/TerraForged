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

package com.terraforged.mod.server.command.search.condition;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.heightmap.Heightmap;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.server.command.search.Search;
import net.minecraft.util.math.BlockPos;

public class VolcanoMatch implements SearchCondition {

    private final Heightmap heightmap;
    private final LocalSearch search;

    public VolcanoMatch(Terrain terrain, Heightmap heightmap) {
        this.heightmap = heightmap;
        this.search = new LocalSearch(new BlockPos.Mutable(), heightmap);
    }

    @Override
    public long test(Cell cell, int x, int z) {
        int centerX = PosUtil.unpackLeft(cell.terrainRegionCenter);
        int centerY = PosUtil.unpackRight(cell.terrainRegionCenter);
        heightmap.apply(cell, centerX, centerY);

        if (cell.terrain == TerrainType.VOLCANO_PIPE) {
            return cell.terrainRegionCenter;
        }

        if (cell.terrain == TerrainType.VOLCANO) {
            search.center.setPos(centerX, 0, centerY);
            BlockPos result = search.get();
            if (result != BlockPos.ZERO) {
                return PosUtil.pack(result.getX(), result.getZ());
            }
        }

        return SearchCondition.NO_MATCH;
    }

    private static class LocalSearch extends Search {

        private final Heightmap heightmap;
        private final BlockPos.Mutable center;

        private final Cell cell = new Cell();

        public LocalSearch(BlockPos.Mutable center, Heightmap heightmap) {
            super(center, 0, 400);
            this.center = center;
            this.heightmap = heightmap;
        }

        @Override
        public int getSpacing() {
            return 2;
        }

        @Override
        public boolean test(BlockPos pos) {
            heightmap.apply(cell, pos.getX(), pos.getZ());
            return cell.terrain == TerrainType.VOLCANO_PIPE;
        }
    }
}
