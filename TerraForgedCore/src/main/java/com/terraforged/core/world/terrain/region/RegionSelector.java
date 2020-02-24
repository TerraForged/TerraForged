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

package com.terraforged.core.world.terrain.region;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.TerrainPopulator;
import me.dags.noise.util.NoiseUtil;

import java.util.LinkedList;
import java.util.List;

public class RegionSelector implements Populator {

    private final int maxIndex;
    private final Populator[] nodes;

    public RegionSelector(List<Populator> populators) {
        this.nodes = getWeightedArray(populators);
        this.maxIndex = nodes.length - 1;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float y) {
        get(cell.region).apply(cell, x, y);
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float y) {
        get(cell.region).tag(cell, x, y);
    }

    public Populator get(float identity) {
        int index = NoiseUtil.round(identity * maxIndex);
        return nodes[index];
    }

    private static Populator[] getWeightedArray(List<Populator> modules) {
        float smallest = Float.MAX_VALUE;
        for (Populator p : modules) {
            if (p instanceof TerrainPopulator) {
                smallest = Math.min(smallest, ((TerrainPopulator) p).getType().getWeight());
            } else {
                smallest = Math.min(smallest, 1);
            }
        }

        List<Populator> result = new LinkedList<>();
        for (Populator p : modules) {
            int count;
            if (p instanceof TerrainPopulator) {
                count = Math.round(((TerrainPopulator) p).getType().getWeight() / smallest);
            } else {
                count = Math.round(1 / smallest);
            }
            while (count-- > 0) {
                result.add(p);
            }
        }

        if (result.isEmpty()) {
            return modules.toArray(new Populator[0]);
        }

        return result.toArray(new Populator[0]);
    }
}
