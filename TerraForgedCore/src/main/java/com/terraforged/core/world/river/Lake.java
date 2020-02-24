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

package com.terraforged.core.world.river;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.TerrainPopulator;
import com.terraforged.core.world.terrain.Terrains;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;

public class Lake extends TerrainPopulator {

    private static final int VALLEY_2 = River.VALLEY_WIDTH * River.VALLEY_WIDTH;

    private final float lakeDistance2;
    private final float valleyDistance2;
    private final float bankAlphaMin;
    private final float bankAlphaMax;
    private final float bankAlphaRange;
    private final Vec2f center;
    private final LakeConfig config;
    private final Terrains terrains;

    public Lake(Vec2f center, float radius, LakeConfig config, Terrains terrains) {
        super(Source.ZERO, terrains.lake);
        this.center = center;
        this.config = config;
        this.bankAlphaMin = config.bankMin;
        this.bankAlphaMax = Math.min(1, bankAlphaMin + 0.275F);
        this.bankAlphaRange = bankAlphaMax - bankAlphaMin;
        this.lakeDistance2 = radius * radius;
        this.valleyDistance2 = VALLEY_2 - lakeDistance2;
        this.terrains = terrains;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float z) {
        float distance2 = getDistance2(x, z);
        if (distance2 > VALLEY_2) {
            return;
        }

        float bankHeight = getBankHeight(cell);
        if (distance2 > lakeDistance2) {
            if (cell.value < bankHeight) {
                return;
            }
            float valleyAlpha = 1F - ((distance2 - lakeDistance2) / valleyDistance2);
            cell.value = NoiseUtil.lerp(cell.value, bankHeight, valleyAlpha);
            return;
        }

        cell.value = Math.min(bankHeight, cell.value);

        if (distance2 < lakeDistance2) {
            float depthAlpha = 1F - (distance2 / lakeDistance2);
            float lakeDepth = Math.min(cell.value, config.depth);
            cell.value = NoiseUtil.lerp(cell.value, lakeDepth, depthAlpha);
            cell.tag = terrains.lake;
        }
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float z) {
        cell.tag = terrains.lake;
    }

    private float getDistance2(float x, float z) {
        float dx = center.x - x;
        float dz = center.y - z;
        return (dx * dx + dz * dz);
    }

    private float getBankHeight(Cell<Terrain> cell) {
        // scale bank height based on elevation of the terrain (higher terrain == taller banks)
        float bankHeightAlpha = NoiseUtil.map(cell.value, bankAlphaMin, bankAlphaMax, bankAlphaRange);
        // lerp between the min and max heights
        return NoiseUtil.lerp(config.bankMin, config.bankMax, bankHeightAlpha);
    }
}
