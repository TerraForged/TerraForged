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

package com.terraforged.core.world.rivermap.lake;

import com.terraforged.core.settings.RiverSettings;
import com.terraforged.core.world.heightmap.Levels;

public class LakeConfig {

    public final float depth;
    public final float chance;
    public final float sizeMin;
    public final float sizeMax;
    public final float bankMin;
    public final float bankMax;
    public final float distanceMin;
    public final float distanceMax;

    private LakeConfig(Builder builder) {
        depth = builder.depth;
        chance = builder.chance;
        sizeMin = builder.sizeMin;
        sizeMax = builder.sizeMax;
        bankMin = builder.bankMin;
        bankMax = builder.bankMax;
        distanceMin = builder.distanceMin;
        distanceMax = builder.distanceMax;
    }

    public static LakeConfig of(RiverSettings.Lake settings, Levels levels) {
        Builder builder = new Builder();
        builder.chance = settings.chance;
        builder.sizeMin = settings.sizeMin;
        builder.sizeMax = settings.sizeMax;
        builder.depth = levels.water(-settings.depth);
        builder.distanceMin = settings.minStartDistance;
        builder.distanceMax = settings.maxStartDistance;
        builder.bankMin = levels.water(settings.minBankHeight);
        builder.bankMax = levels.water(settings.maxBankHeight);
        return new LakeConfig(builder);
    }

    public static class Builder {
        public float chance;
        public float depth = 10;
        public float sizeMin = 30;
        public float sizeMax = 100;
        public float bankMin = 1;
        public float bankMax = 8;
        public float distanceMin = 0.025F;
        public float distanceMax = 0.05F;
    }
}
