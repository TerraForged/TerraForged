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

package com.terraforged.mod.feature.tree;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SaplingFeature {

    private final List<Feature<NoFeatureConfig>> normal;
    private final List<Feature<NoFeatureConfig>> giant;

    public SaplingFeature(SaplingConfig config) {
        this.normal = new ArrayList<>(config.getNormal());
        this.giant = new ArrayList<>(config.getGiant());
    }

    public Feature<NoFeatureConfig> nextNormal(Random random) {
        if (normal.isEmpty()) {
            return null;
        }
        return normal.get(random.nextInt(normal.size()));
    }

    public Feature<NoFeatureConfig> nextGiant(Random random) {
        if (giant.isEmpty()) {
            return null;
        }
        return giant.get(random.nextInt(giant.size()));
    }

    public int getNormalCount() {
        return normal.size();
    }

    public int getGiantCount() {
        return giant.size();
    }
}
