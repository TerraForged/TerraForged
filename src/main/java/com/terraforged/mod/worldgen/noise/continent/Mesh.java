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

import java.util.ArrayList;
import java.util.List;

public class Mesh {
    private static final ThreadLocal<Edge[]> LOCAL_EDGES = ThreadLocal.withInitial(() -> new Edge[8]);

    private static final int[] NEIGHBOURHOOD = {
            // North row
            -1, -1,
            +0, -1,
            +1, -1,
            // East col
            +1, +0,
            +1, +1,
            // South Row
            +0, +1,
            -1, +1,
            // West col
            -1, +0
    };

    public final int hash;
    public final int cx, cy;
    public final float px, py;
    public final List<Edge> rivers = new ArrayList<>();

    public Mesh(int hash, int cx, int cy, float px, float py) {
        this.hash = hash;
        this.cx = cx;
        this.cy = cy;
        this.px = px;
        this.py = py;
    }

    public void init(int seed, float jitter, float threshold) {
        var edges = LOCAL_EDGES.get();

        collectEdges(seed, jitter, edges);

        cullEdges(edges);

        connectEdges(edges);

        addEdges(threshold, edges);

        RiverGen.gen(this, edges);
    }

    protected void collectEdges(int seed, float jitter, Edge[] edges) {
        for (int i = 0, j = 0; i < NEIGHBOURHOOD.length; j++) {
            int cx = this.cx + NEIGHBOURHOOD[i++];
            int cy = this.cy + NEIGHBOURHOOD[i++];

            int hash = MathUtil.hash(seed, cx, cy);
            float px = MathUtil.getPosX(hash, cx, jitter);
            float py = MathUtil.getPosY(hash, cy, jitter);

            float mx = (this.px + px) * 0.5f;
            float my = (this.py + py) * 0.5f;

            float nx = -(this.py - py);
            float ny = (this.px - px);

            edges[j] = new Edge(hash, mx, my, mx + nx, my + ny);
        }
    }

    protected void cullEdges(Edge[] edges) {
        cullEdge(edges, 7, 0, 1);
        cullEdge(edges, 1, 2, 3);
        cullEdge(edges, 3, 4, 5);
        cullEdge(edges, 5, 6, 7);
    }

    protected void cullEdge(Edge[] edges, int x, int y, int z) {
        var a = edges[x];
        var b = edges[y];
        var c = edges[z];

        long p0 = MathUtil.getIntersection(px, py, b.ax, b.ay, b.bx, b.by);
        float x0 = PosUtil.unpackLeftf(p0);
        float y0 = PosUtil.unpackRightf(p0);

        long p1 = MathUtil.getIntersection(a.ax, a.ay, a.bx, a.by, c.ax, c.ay, c.bx, c.by);
        float x1 = PosUtil.unpackLeftf(p1);
        float y1 = PosUtil.unpackRightf(p1);

        float d0 = NoiseUtil.dist2(this.px, this.py, x0, y0);
        float d1 = NoiseUtil.dist2(this.px, this.py, x1, y1);

        if (d0 > d1) edges[y] = Edge.NONE;
    }

    protected void connectEdges(Edge[] edges) {
        int previous = last(edges);
        if (previous == -1) return;

        for (int next = next(-1, edges); next < edges.length; ) {
            var a = edges[previous];
            var b = edges[next];

            long pos = MathUtil.getIntersection(a.ax, a.ay, a.bx, a.by, b.ax, b.ay, b.bx, b.by);

            if (pos != Long.MAX_VALUE) {
                float x = PosUtil.unpackLeftf(pos);
                float y = PosUtil.unpackRightf(pos);

                a.bx = x;
                a.by = y;

                b.ax = x;
                b.ay = y;
            }

            previous = next;
            next = next(next, edges);
        }
    }

    protected static int next(int i, Edge[] edges) {
        while (++i < edges.length) {
            if (edges[i] != Edge.NONE) return i;
        }
        return edges.length;
    }

    protected static int last(Edge[] edges) {
        for (int i = edges.length - 1; i >= 0; i--) {
            if (edges[i] != Edge.NONE) return i;
        }
        return -1;
    }

    protected void addEdges(float threshold, Edge[] edges) {
        int density = ContinentGenerator.getDensity(hash, threshold);

        for (int i = 0; i < edges.length; i++) {
            if (ContinentGenerator.getDensity(edges[i].hash, threshold) == density) {
                edges[i] = Edge.NONE;
            }
        }
    }

    public static class Edge {
        public static final Edge NONE = new Edge(0, 0, 0, 0);

        public int hash;
        public float ax, ay, bx, by;

        public Edge(int hash, float ax, float ay, float bx, float by) {
            this(ax, ay, bx, by);
            this.hash = hash;
        }

        public Edge(float ax, float ay, float bx, float by) {
            this.ax = ax;
            this.ay = ay;
            this.bx = bx;
            this.by = by;
        }

        public float distance(float x, float y) {
            float dx = bx - ax;
            float dy = by - ay;

            float v = (x - ax) * dx + (y - ay) * dy;
            v /= dx * dx + dy * dy;

            v = MathUtil.clamp(v, 0, 1);

            float px = ax + dx * v;
            float py = ay + dy * v;

            return NoiseUtil.dist2(x, y, px, py);
        }

        public float distance(float x, float y, float ra, float rb) {
            float dx = bx - ax;
            float dy = by - ay;

            float v = (x - ax) * dx + (y - ay) * dy;
            v /= dx * dx + dy * dy;

            v = MathUtil.clamp(v, 0, 1);

            float px = ax + dx * v;
            float py = ay + dy * v;

            float d2 = NoiseUtil.dist2(x, y, px, py);
            float r2 = NoiseUtil.lerp(ra * ra, rb * rb, v);

            return NoiseUtil.clamp(d2 / r2, 0, 1);
        }
    }
}
