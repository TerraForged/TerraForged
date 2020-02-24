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

package com.terraforged.core.world.geology;

import com.terraforged.core.util.Seed;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Strata<T> {

    private final float[] depthBuffer;
    private final Module heightMod;
    private final List<Stratum<T>> strata;

    private Strata(Module heightMod, List<Stratum<T>> strata) {
        this.strata = strata;
        this.heightMod = heightMod;
        this.depthBuffer = new float[strata.size()];
    }

    public boolean downwards(final int x, final int y, final int z, Stratum.Visitor<T> visitor) {
        int py = y;
        T last = null;
        float sum = getDepth(x, z);
        for (int i = 0; i < strata.size(); i++) {
            float depth = depthBuffer[i] / sum;
            int height = NoiseUtil.round(depth * y);
            T value = strata.get(i).getValue();
            last = value;
            for (int dy = 0; dy < height; dy++) {
                if (py <= y) {
                    boolean cont = visitor.visit(py, value);
                    if (!cont) {
                        return false;
                    }
                }
                if (--py < 0) {
                    return false;
                }
            }
        }
        if (last != null) {
            while (py > 0) {
                visitor.visit(py, last);
                py--;
            }
        }
        return true;
    }

    public boolean upwards(int x, int y, int z, Stratum.Visitor<T> visitor) {
        int py = 0;
        float sum = getDepth(x, z);
        for (int i = strata.size() - 1; i > 0; i--) {
            float depth = depthBuffer[i] / sum;
            int height = NoiseUtil.round(depth * y);
            T value = strata.get(i).getValue();
            for (int dy = 0; dy < height; dy++) {
                boolean cont = visitor.visit(py, value);
                if (!cont) {
                    return false;
                }
                if (++py > y) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getYOffset(int x, int z) {
        return (int) (64 * heightMod.getValue(x, z));
    }

    private float getDepth(int x, int z) {
        float sum = 0F;
        for (int i = 0; i < strata.size(); i++) {
            float depth = strata.get(i).getDepth(x, z);
            sum += depth;
            depthBuffer[i] = depth;
        }
        return sum;
    }

    public static <T> Builder<T> builder(int seed, me.dags.noise.source.Builder noise) {
        return new Builder<>(seed, noise);
    }

    public static class Builder<T> {

        private final Seed seed;
        private final me.dags.noise.source.Builder noise;
        private final List<Stratum<T>> strata = new LinkedList<>();

        public Builder(int seed, me.dags.noise.source.Builder noise) {
            this.seed = new Seed(seed);
            this.noise = noise;
        }

        public Builder<T> add(T material, double depth) {
            Module module = noise.seed(seed.next()).perlin().scale(depth);
            strata.add(Stratum.of(material, module));
            return this;
        }

        public Builder<T> add(Source type, T material, double depth) {
            Module module = noise.seed(seed.next()).build(type).scale(depth);
            strata.add(Stratum.of(material, module));
            return this;
        }

        public Strata<T> build() {
            Module height = Source.cell(seed.next(), 100);
            return new Strata<>(height, new ArrayList<>(strata));
        }
    }
}
