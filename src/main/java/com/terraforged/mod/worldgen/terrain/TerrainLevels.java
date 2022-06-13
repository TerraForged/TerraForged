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

package com.terraforged.mod.worldgen.terrain;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.terraforged.mod.data.codec.Codecs;
import com.terraforged.mod.data.codec.LazyCodec;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.worldgen.noise.NoiseLevels;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.function.Supplier;

public class TerrainLevels {
    public static final Codec<TerrainLevels> CODEC = LazyCodec.record(instance -> instance.group(
            Codecs.opt("auto_scale", true, Codec.BOOL).forGetter(l -> l.noiseLevels.auto),
            Codecs.opt("horizontal_scale", 1F, Codec.floatRange(0F, 10F)).forGetter(l -> l.noiseLevels.scale),
            Codec.intRange(Limits.MIN_MIN_Y, Limits.MAX_MIN_Y).fieldOf("min_y").forGetter(l -> l.minY),
            Codec.intRange(Limits.MIN_MAX_Y, Limits.MAX_MAX_Y).fieldOf("max_y").forGetter(l -> l.maxY),
            Codec.intRange(Limits.MAX_MIN_Y, Limits.MAX_MAX_Y).fieldOf("base_height").forGetter(l -> l.baseHeight),
            Codec.intRange(Limits.MIN_SEA_LEVEL, Limits.MAX_SEA_LEVEL).fieldOf("sea_level").forGetter(l -> l.seaLevel),
            Codec.intRange(Limits.MIN_SEA_FLOOR, Limits.MAX_SEA_FLOOR).fieldOf("sea_floor").forGetter(l -> l.seaFloor)
    ).apply(instance, TerrainLevels::new));

    public static final Supplier<TerrainLevels> DEFAULT = Suppliers.memoize(() -> new TerrainLevels(true, Defaults.SCALE, Defaults.MIN_Y, Defaults.MAX_Y, Defaults.MAX_BASE_HEIGHT, Defaults.SEA_LEVEL, Defaults.SEA_FLOOR));

    public final int minY;
    public final int maxY; // Exclusive max block index
    public final int baseHeight;
    public final int seaFloor;
    public final int seaLevel; // Inclusive index of highest water block
    public final NoiseLevels noiseLevels;

    public TerrainLevels() {
        this.minY = -Defaults.MIN_Y;
        this.maxY = Defaults.MAX_Y;
        this.baseHeight = Defaults.MAX_BASE_HEIGHT;
        this.seaFloor = Defaults.SEA_FLOOR;
        this.seaLevel = Defaults.SEA_LEVEL;
        this.noiseLevels = new NoiseLevels(false, Defaults.SCALE, seaLevel, seaFloor, maxY, baseHeight);
    }

    public TerrainLevels(boolean autoScale, float scale, int minY, int maxY, int baseHeight, int seaLevel, int seaFloor) {
        this.minY = MathUtil.clamp(minY, Limits.MIN_MIN_Y, Limits.MAX_MIN_Y);
        this.maxY = MathUtil.clamp(maxY, Limits.MIN_MAX_Y, Limits.MAX_MAX_Y);
        this.seaLevel = MathUtil.clamp(seaLevel, Limits.MIN_SEA_LEVEL, maxY >> 1);
        this.seaFloor = MathUtil.clamp(seaFloor, this.minY, this.seaLevel - 1);
        this.baseHeight = MathUtil.clamp(baseHeight, this.seaLevel, this.maxY);
        this.noiseLevels = new NoiseLevels(autoScale, scale, this.seaLevel, this.seaFloor, this.maxY, this.baseHeight);
    }

    public TerrainLevels copy() {
        return new TerrainLevels(noiseLevels.auto, noiseLevels.scale, minY, maxY, baseHeight, seaLevel, seaFloor);
    }

    public float getScaledHeight(float heightNoise) {
        return heightNoise * maxY;
    }

    public float getScaledBaseLevel(float waterLevelNoise) {
        return noiseLevels.toHeightNoise(waterLevelNoise, 0f) * maxY;
    }

    public int getHeight(float scaledHeight) {
        return NoiseUtil.floor(scaledHeight);
    }

    @Override
    public String toString() {
        return "TerrainLevels{" +
                "minY=" + minY +
                ", maxY=" + maxY +
                ", seaFloor=" + seaFloor +
                ", seaLevel=" + seaLevel +
                ", noiseLevels=" + noiseLevels +
                '}';
    }

    public static int getWaterLevel(int x, int z, int seaLevel, TerrainData terrainData) {
        float river = terrainData.getRiver().get(x, z);
        var terrain = terrainData.getTerrain().get(x, z);
        return (terrain.isRiver() || terrain.isLake()) && river == 0f ? terrainData.getBaseHeight(x, z) : seaLevel;
    }

    public static class Limits {
        public static final int MIN_MIN_Y = DimensionType.MIN_Y;
        public static final int MAX_MIN_Y = 0;
        public static final int MIN_SEA_LEVEL = 32;
        public static final int MAX_SEA_LEVEL = DimensionType.Y_SIZE;
        public static final int MIN_SEA_FLOOR = 0;
        public static final int MAX_SEA_FLOOR = MAX_SEA_LEVEL;
        public static final int MIN_MAX_Y = 128;
        public static final int MAX_MAX_Y = DimensionType.Y_SIZE;
    }

    public static class Defaults {
        public static final float SCALE = 1;
        public static final int MIN_Y = -64;
        public static final int MAX_Y = 480;
        public static final int MAX_BASE_HEIGHT = 128;
        public static final int SEA_LEVEL = 62;
        public static final int SEA_FLOOR = SEA_LEVEL - 40;
        public static final int LEGACY_GEN_DEPTH = 256;
    }
}
