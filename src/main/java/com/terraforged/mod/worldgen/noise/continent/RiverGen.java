package com.terraforged.mod.worldgen.noise.continent;/*
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

import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.noise.util.NoiseUtil;

import java.util.List;

public class RiverGen {
    public static void gen(Mesh mesh, Mesh.Edge[] edges) {
        int i = 1;

        for (var edge : edges) {
            if (edge == Mesh.Edge.NONE) continue;

            int hash = MathUtil.hash(mesh.hash, i++);

            float count = 2 + MathUtil.rand(hash) * 2;

            add(mesh.px, mesh.py, count, edge, mesh.rivers);
        }
    }

    protected static void add(float px, float py, float count, Mesh.Edge edge, List<Mesh.Edge> list) {
        float pad = 0.1f;
        float range = 1.0f - (pad * 2);

        float dx = edge.bx - edge.ax;
        float dy = edge.by - edge.ay;

        float d = NoiseUtil.sqrt(dx * dx + dy * dy);
        float nx = -dy / d;
        float ny = dx / d;

        count *= (d / 1.5f);

        for (int i = 0; i <= count; i++) {
            float alpha = pad + (i / count) * range;

            float ax = edge.ax + dx * alpha;
            float ay = edge.ay + dy * alpha;
            float len = getLength(ax, ay, px, py, edge) * 2.0f;

            float bx = ax + nx * len;
            float by = ay + ny * len;

            if (valid(ax, ay, bx, by, list)) {
                list.add(new Mesh.Edge(ax, ay, bx, by));
            }
        }
    }
    protected static float getLength(float ax, float ay, float px, float py, Mesh.Edge edge) {
        long p0 = MathUtil.getIntersection(ax, ay, px, py, edge.ax, edge.ay);
        long p1 = MathUtil.getIntersection(ax, ay, px, py, edge.bx, edge.by);

        float d0 = NoiseUtil.dist2(ax, ay, PosUtil.unpackLeftf(p0), PosUtil.unpackRightf(p0));
        float d1 = NoiseUtil.dist2(ax, ay, PosUtil.unpackLeftf(p1), PosUtil.unpackRightf(p1));

        return NoiseUtil.sqrt(Math.min(d0, d1));
    }

    protected static boolean valid(float ax, float ay, float bx, float by, List<Mesh.Edge> list) {
        for (int i = 0; i < list.size(); i++) {
            var edge = list.get(i);

            if (MathUtil.intersects(ax, ay, bx, by, edge.ax, edge.ay, edge.bx, edge.by)) {
                return false;
            }
        }
        return true;
    }
}
