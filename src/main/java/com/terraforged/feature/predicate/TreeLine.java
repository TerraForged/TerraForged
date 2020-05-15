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

package com.terraforged.feature.predicate;

import com.terraforged.chunk.TerraContext;
import com.terraforged.fm.predicate.FeaturePredicate;
import com.terraforged.world.climate.Climate;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;

public class TreeLine implements FeaturePredicate {

    private final int worldHeight;
    private final Climate climate;

    public TreeLine(TerraContext context) {
        this.worldHeight = context.levels.worldHeight;
        this.climate = context.heightmap.getClimate();
    }

    @Override
    public boolean test(IChunk chunk, Biome biome) {
        int x = chunk.getPos().getXStart() + 8;
        int z = chunk.getPos().getZStart() + 8;
        int treeline = getTreeline(x, z);
        int y = chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, 8, 8);
        return y < treeline;
    }

    private int getTreeline(int x, int z) {
        return (int) (climate.getTreeLine(x, z) * worldHeight);
    }
}
