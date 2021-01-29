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

public class ExactTerrainMatch implements SearchCondition {

    private final Terrain terrain;
    private final Heightmap heightmap;

    public ExactTerrainMatch(Terrain terrain, Heightmap heightmap) {
        this.terrain = terrain;
        this.heightmap = heightmap;
    }

    @Override
    public long test(Cell cell, int x, int z) {
        heightmap.apply(cell, x, z);
        if (cell.terrain == terrain) {
            return PosUtil.pack(x, z);
        }
        return SearchCondition.NO_MATCH;
    }
}
