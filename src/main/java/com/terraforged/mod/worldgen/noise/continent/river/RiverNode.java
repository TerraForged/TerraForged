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

package com.terraforged.mod.worldgen.noise.continent.river;

import com.terraforged.mod.worldgen.noise.continent.cell.CellPoint;
import com.terraforged.noise.source.Line;
import com.terraforged.noise.util.NoiseUtil;

public record RiverNode(float ax, float ay, float bx, float by, float ah, float bh, float ar, float br) {
    public float getProjection(float x, float y) {
        float dx = bx - ax;
        float dy = by - ay;
        float v = (x - ax) * dx + (y - ay) * dy;
        v /= dx * dx + dy * dy;
        return v < 0 ? 0 : v > 1 ? 1 : v;
    }

    public float getDistance2(float x, float y, float t) {
        float tx = getX(t);
        float ty = getY(t);
        return Line.dist2(x, y, tx, ty);
    }

    public float getDistance(float x, float y, float t) {
        float d2 = getDistance2(x, y, t);
        return getDistance(t, d2);
    }

    public float getDistance(float t, float d2) {
        float tr = getRadius(t);

        return d2 >= tr * tr ? 0.0f : 1.0f - NoiseUtil.sqrt(d2) / tr;
    }

    public float getX(float t) {
        return ax + t * (bx - ax);
    }

    public float getY(float t) {
        return ay + t * (by - ay);
    }

    public float getHeight(float t) {
        return ah + t * (bh - ah);
    }

    public float getRadius(float t) {
        return ar + t * (br - ar);
    }

    public static RiverNode of(CellPoint a, CellPoint b, float ah, float bh, float ar, float br) {
        return new RiverNode(a.px, a.py, b.px, b.py, ah, bh, ar, br);
    }
}
