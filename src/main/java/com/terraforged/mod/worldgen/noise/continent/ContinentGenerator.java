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

package com.terraforged.mod.worldgen.noise.continent;

import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.heightmap.ControlPoints;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.util.ObjectPool;
import com.terraforged.mod.util.SpiralIterator;
import com.terraforged.mod.util.map.LossyCache;
import com.terraforged.mod.worldgen.noise.NoiseLevels;
import com.terraforged.mod.worldgen.noise.continent.cell.CellPoint;
import com.terraforged.mod.worldgen.noise.continent.cell.CellShape;
import com.terraforged.mod.worldgen.noise.continent.cell.CellSource;
import com.terraforged.mod.worldgen.noise.continent.river.RiverGenerator;
import com.terraforged.mod.worldgen.noise.continent.shape.ShapeGenerator;
import com.terraforged.noise.util.Vec2f;

public class ContinentGenerator {
    protected static final int SAMPLE_SEED_OFFSET = 6569;
    protected static final int VALID_SPAWN_RADIUS = 3;
    protected static final int SPAWN_SEARCH_RADIUS = 100_000;
    protected static final int CELL_POINT_CACHE_SIZE = 2048;

    public final int seed;
    public final float jitter;
    public final int sampleSeed;
    public final NoiseLevels levels;
    public final ControlPoints controlPoints;

    public final CellShape cellShape;
    public final CellSource cellSource;
    public final RiverGenerator riverGenerator;
    public final ShapeGenerator shapeGenerator;

    private final ObjectPool<CellPoint> cellPool = new ObjectPool<>(CELL_POINT_CACHE_SIZE, CellPoint::new);
    private final LossyCache<CellPoint> cellCache = new LossyCache.Concurrent<>(CELL_POINT_CACHE_SIZE, CellPoint[]::new, cellPool::restore);

    public ContinentGenerator(ContinentConfig config, NoiseLevels levels, ControlPoints controlPoints) {
        this.levels = levels;
        this.controlPoints = controlPoints;

        this.seed = config.shape.seed0;
        this.sampleSeed = config.shape.seed1 + SAMPLE_SEED_OFFSET;
        this.jitter = config.shape.jitter;
        this.cellShape = config.shape.cellShape;
        this.cellSource = config.shape.cellSource;
        this.riverGenerator = new RiverGenerator(this, config);
        this.shapeGenerator = new ShapeGenerator(this, config, controlPoints);
    }

    public Vec2f getWorldOffset() {
        var iterator = new SpiralIterator(0, 0, 0, SPAWN_SEARCH_RADIUS);
        var cell = new CellPoint();

        while (iterator.hasNext()) {
            long pos = iterator.next();
            computeCell(pos, 0, 0, cell);

            if (shapeGenerator.getThresholdValue(cell) == 0) {
                continue;
            }

            float px = cell.px;
            float py = cell.py;
            if (isValidSpawn(pos, VALID_SPAWN_RADIUS, cell)) {
                return new Vec2f(px, py);
            }
        }

        return Vec2f.ZERO;
    }

    public CellPoint getCell(int cx, int cy) {
        long index = PosUtil.pack(cx, cy);
        return cellCache.computeIfAbsent(index, this::computeCell);
    }

    private CellPoint computeCell(long index) {
        return computeCell(index, 0, 0, cellPool.take());
    }

    private CellPoint computeCell(long index, int ox, int oy, CellPoint cell) {
        int cx = PosUtil.unpackLeft(index) + ox;
        int cy = PosUtil.unpackRight(index) + oy;

        int hash = MathUtil.hash(seed, cx, cy);
        float px = cellShape.getCellX(hash, cx, cy, jitter);
        float py = cellShape.getCellY(hash, cx, cy, jitter);

        cell.cx = cx;
        cell.cy = cy;
        cell.px = px;
        cell.py = py;

        sampleCell(sampleSeed, px, py,  cellSource,2, 0.075f, 2.5f, 0.3f, cell);

        return cell;
    }

    private static void sampleCell(int seed, float x, float y, CellSource cellSource, int octaves, float frequency, float lacunarity, float gain, CellPoint cell) {
        x *= frequency;
        y *= frequency;

        float sum = cellSource.getValue(seed, x, y);
        float amp = 1.0F;
        float sumAmp = amp;

        cell.noise0 = sum;

        for(int i = 1; i < octaves; ++i) {
            amp *= gain;
            x *= lacunarity;
            y *= lacunarity;

            sum += cellSource.getValue(seed, x, y) * amp;
            sumAmp += amp;
        }

        cell.noise = sum / sumAmp;
    }

    private boolean isValidSpawn(long pos, int radius, CellPoint cell) {
        int radius2 = radius * radius;

        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int d2 = dx * dx + dy * dy;

                if (dy < 1 || d2 >= radius2) continue;

                computeCell(pos, dx, dy, cell);

                if (shapeGenerator.getThresholdValue(cell) == 0) {
                    return false;
                }
            }
        }

        return true;
    }
}
