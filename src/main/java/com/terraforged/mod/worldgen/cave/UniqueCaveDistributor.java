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

package com.terraforged.mod.worldgen.cave;

import com.terraforged.noise.Module;
import com.terraforged.noise.util.N2DUtil;
import com.terraforged.noise.util.NoiseUtil;

import java.awt.*;

public class UniqueCaveDistributor implements Module {
    private final int seed;
    private final float frequency;
    private final float jitter;
    private final float density;

    public UniqueCaveDistributor(int seed, float frequency, float jitter, float density) {
        this.seed = seed;
        this.frequency = frequency;
        this.jitter = jitter;
        this.density = density;
    }

    @Override
    public float getValue(int seed, float x, float y) {
        x *= frequency;
        y *= frequency;

        int maxX = NoiseUtil.floor(x) + 1;
        int maxY = NoiseUtil.floor(y) + 1;

        int cellX = maxX - 1;
        int cellY = maxY - 1;

        float distA = Float.POSITIVE_INFINITY;
        float distB = Float.POSITIVE_INFINITY;
        for (int cy = maxY - 2; cy <= maxY; cy++) {
            float ox = (cy & 1) * 0.5F;

            for (int cx = maxX - 2; cx <= maxX; cx++) {
                var vec = NoiseUtil.cell(seed, cx, cy);
                float px = cx + ox + (vec.x * jitter);
                float py = cy + vec.y * jitter;
                float d2 = NoiseUtil.dist2(x, y, px, py);
                if (d2 < distA) {
                    distB = distA;
                    distA = d2;
                    cellX = cx;
                    cellY = cy;
                } else if (d2 < distB) {
                    distB = d2;
                }
            }
        }

        if (cellValue(cellX, cellY) > density) {
            return 0F;
        }

        return 1F - NoiseUtil.sqrt(distA / distB);
    }

    private float cellValue(int cellX, int cellY) {
        float noise = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return (1 + noise) * 0.5F;
    }

    public static void main(String[] args) {
        var noise = new UniqueCaveDistributor(123, 0.01F, 0.75F, 0.1F)
                .clamp(0.2, 1.0)
                .map(0, 1)
                .warp(12312, 30, 1, 20);

        N2DUtil.display(1000, 1000, (x, y, img) -> {
            return Color.HSBtoRGB(0, 0, noise.getValue(2132, x, y));
        }).setVisible(true);
    }
}
