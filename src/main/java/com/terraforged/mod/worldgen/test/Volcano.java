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

package com.terraforged.mod.worldgen.test;

import net.minecraft.util.Mth;

import java.util.Arrays;

public class Volcano {
    public static int toHeightValue(double height) {
        return 64 + Mth.floor(height * 0.5);
    }

    public static Value getHighest(int x, int z, VolcanoConfig config, Cache cache) {
        var maxValue = cache.value0.reset();
        var value = cache.value1.reset();

        for (int i = 0; i < cache.size(); ++i) {
            var point = cache.at(i);
            if (!point.valid()) continue;

            evalPoint(x, z, point, value.reset(), config);

            boolean higher = value.height > maxValue.height;

            if (maxValue.mouth ? value.mouth && higher : value.mouth || higher) {
                maxValue.height = value.height;
                maxValue.mouth = value.mouth;
                maxValue.hash = point.hash;
            }
        }

        return maxValue;
    }

    private static void evalPoint(int x, int y, Point point, Value value, VolcanoConfig config) {
        double alpha = getDistanceNoise(x, y, point, value, config);

        if (Double.isNaN(alpha)) return;

        double h0 = 0;
        double h1 = config.height1().get(Noise.rand(point.hash, Noise.HEIGHT_1));

        if (value.mouth) {
            h0 = config.height0().get(Noise.rand(point.hash, Noise.HEIGHT_0));
        } else {
            // Curve outer slope
            alpha *= alpha;
        }

        double height = Mth.lerp(alpha, h0, h1);

        value.height = toHeightValue(height);
    }

    private static double getDistanceNoise(int x, int y, Point point, Value value, VolcanoConfig config) {
        double distance2 = Mth.lengthSquared(point.x - x, point.y - y);
        double outer = config.radius2().get(Noise.rand(point.hash, Noise.RADIUS_2));
        if (distance2 >= outer * outer) return Double.NaN;

        double origin = 1.0;
        double inner = config.radius1().get(Noise.rand(point.hash, Noise.RADIUS_1));
        if (distance2 < inner * inner) {
            value.mouth = true;

            outer = inner;
            inner = config.radius0().get(Noise.rand(point.hash, Noise.RADIUS_0));

            if (distance2 <= inner * inner) return 0.0;

            return (Math.sqrt(distance2) - inner) / (outer - inner);
        }

        return origin - (Math.sqrt(distance2) - inner) / (outer - inner);
    }

    public static <T> void collectPoints(long seed,
                                         int chunkX, int chunkZ,
                                         T context,
                                         VolcanoConfig config,
                                         Cache cache,
                                         VolcanoPredicate<T> filter) {
        // Min/max world-space coords
        int x0 = chunkX << 4;
        int y0 = chunkZ << 4;
        int x1 = x0 + 15;
        int y1 = y0 + 15;

        // Converted to grid space
        double frequency = 1.0 / config.scale();
        double fx0 = x0 * frequency;
        double fy0 = y0 * frequency;
        double fx1 = x1 * frequency;
        double fy1 = y1 * frequency;

        // Converted to grid lines
        int minX = Mth.floor(fx0) - 1;
        int minY = Mth.floor(fy0) - 1;
        int maxX = Mth.floor(fx1) + 1;
        int maxY = Mth.floor(fy1) + 1;

        collectPoints(seed, minX, minY, maxX, maxY, frequency, context, config, cache, filter);
    }

    private static <T> void collectPoints(long seed,
                                   int minX, int minY,
                                   int maxX, int maxY,
                                   double frequency,
                                   T context,
                                   VolcanoConfig config,
                                   Cache cache,
                                   VolcanoPredicate<T> filter) {
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                long hash = Noise.mix(seed, x, y);

                // Check if position should be skipped to reduce density
                if (Noise.rand(hash, Noise.DENSITY) > config.density()) continue;

                double px = point(hash, Noise.POINT_X, x, config.jitter());
                double py = point(hash, Noise.POINT_Y, y, config.jitter());

                int posX = Mth.floor(px / frequency);
                int posY = Mth.floor(py / frequency);

                // Check centre is at a valid position
                if (!filter.test(posX, posY, context)) continue;

                var point = cache.next();
                point.x = posX;
                point.y = posY;
                point.hash = hash;
            }
        }
    }

    private static double point(long hash, int hashOffset, int cell, double jitter) {
        return cell + Noise.rand(hash, hashOffset) * jitter;
    }

    public static class Value {
        public long hash = 0;
        public int height = 0;
        public boolean mouth = false;

        public Value reset() {
            hash = 0;
            height = 0;
            mouth = false;
            return this;
        }
    }

    public static class Point {
        public long hash = Long.MAX_VALUE;
        public int x = Integer.MAX_VALUE;
        public int y = Integer.MAX_VALUE;

        public boolean valid() {
            return hash != Long.MAX_VALUE && x != Integer.MAX_VALUE && y != Integer.MAX_VALUE;
        }

        public Point reset() {
            hash = Long.MAX_VALUE;
            x = Integer.MAX_VALUE;
            y = Integer.MAX_VALUE;
            return this;
        }
    }

    public static class Cache {
        protected int size;
        protected Point[] points = new Point[9];

        protected final Value value0 = new Value();
        protected final Value value1 = new Value();

        public Cache() {
            for (int i = 0; i < points.length; ++i) points[i] = new Point();
        }

        public int size() {
            return size;
        }

        public Cache reset() {
            this.size = 0;
            return this;
        }

        public Point at(int index) {
            return points[index];
        }

        public Point next() {
            int index = size;

            ensure(index);
            size++;

            return points[index].reset();
        }

        protected void ensure(int index) {
            if (index < points.length) return;

            int oldLength = points.length;
            int newLength = oldLength << 1;
            points = Arrays.copyOf(points, newLength);

            for (int i = oldLength; i < newLength; ++i) {
                points[i] = new Point();
            }
        }
    }

    public interface VolcanoPredicate<T> {
        boolean test(int x, int y, T context);
    }

    public interface Noise {
        int DENSITY = 6869;

        int POINT_X = 12343;
        int POINT_Y = 16477;

        int RADIUS_0 = 18899;
        int RADIUS_1 = 21701;
        int RADIUS_2 = 26921;

        int HEIGHT_0 = 30047;
        int HEIGHT_1 = 31643;
        int HEIGHT_2 = 33199;

        int FLUID_FILLER = 39761;

        static long mixGamma(long z) {
            z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
            z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
            z = (z ^ (z >>> 33)) | 1L;
            int n = Long.bitCount(z ^ (z >>> 1));
            return (n < 24) ? z ^ 0xaaaaaaaaaaaaaaaaL : z;
        }

        static long mix(long z) {
            z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
            z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
            return z ^ (z >>> 31);
        }

        static long mix(long seed, long offset) {
            return mix(seed + mixGamma(offset));
        }

        static long mix(long seed, int x, int y) {
            long hash = mix(seed, x);
            hash = mix(hash, y);
            return hash;
        }

        static double rand(long hash, int offset) {
            return rand(mix(hash, offset));
        }

        static double rand(long hash) {
            return (hash >>> 11) * 0x1.0p-53;
        }
    }
}
