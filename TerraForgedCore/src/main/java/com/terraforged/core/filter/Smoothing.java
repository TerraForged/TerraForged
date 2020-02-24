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

package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.world.heightmap.Levels;
import me.dags.noise.util.NoiseUtil;

public class Smoothing implements Filter {

    private final int radius;
    private final float rad2;
    private final float strength;
    private final Modifier modifier;

    public Smoothing(Settings settings, Levels levels) {
        this.radius = NoiseUtil.round(settings.filters.smoothing.smoothingRadius + 0.5F);
        this.rad2 = settings.filters.smoothing.smoothingRadius * settings.filters.smoothing.smoothingRadius;
        this.strength = settings.filters.smoothing.smoothingRate;
        this.modifier = Modifier.range(levels.ground(1), levels.ground(120)).invert();
    }

    @Override
    public void apply(Filterable<?> map, int seedX, int seedZ, int iterations) {
        while (iterations-- > 0) {
            apply(map);
        }
    }

    private void apply(Filterable<?> cellMap) {
        int maxZ = cellMap.getSize().total - radius;
        int maxX = cellMap.getSize().total - radius;
        for (int z = radius; z < maxZ; z++) {
            for (int x = radius; x < maxX; x++) {
                Cell<?> cell = cellMap.getCellRaw(x, z);

                float total = 0;
                float weights = 0;

                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        float dist2 = dx * dx + dz * dz;
                        if (dist2 > rad2) {
                            continue;
                        }
                        int px = x + dx;
                        int pz = z + dz;
                        Cell<?> neighbour = cellMap.getCellRaw(px, pz);
                        if (neighbour.isAbsent()) {
                            continue;
                        }
                        float value = neighbour.value;
                        float weight = 1F - (dist2 / rad2);
                        total += (value * weight);
                        weights += weight;
                    }
                }

                if (weights > 0) {
                    float dif = cell.value - (total / weights);
                    cell.value -= modifier.modify(cell, dif * strength);
                }
            }
        }
    }
}
