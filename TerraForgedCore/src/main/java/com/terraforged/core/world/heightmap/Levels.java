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

package com.terraforged.core.world.heightmap;

import com.terraforged.core.settings.GeneratorSettings;
import me.dags.noise.util.NoiseUtil;

public class Levels {

    public final int worldHeight;

    public final float unit;

    // y index of the top-most water block
    public final int waterY;
    private final int groundY;

    // top of the first ground block (ie 1 above ground index)
    public final int groundLevel;
    // top of the top-most water block (ie 1 above water index)
    public final int waterLevel;

    // ground index mapped between 0-1 (default 63 / 256)
    public final float ground;
    // water index mapped between 0-1 (default 62 / 256)
    public final float water;

    public Levels(GeneratorSettings settings) {
        worldHeight = Math.max(1, settings.world.worldHeight);
        unit = NoiseUtil.div(1, worldHeight);

        waterLevel = settings.world.seaLevel;
        groundLevel = waterLevel + 1;

        waterY = Math.min(waterLevel - 1, worldHeight);
        groundY = Math.min(groundLevel - 1, worldHeight);

        ground = NoiseUtil.div(groundY, worldHeight);
        water = NoiseUtil.div(waterY, worldHeight);
    }

    public float scale(int level) {
        return NoiseUtil.div(level, worldHeight);
    }

    public float water(int amount) {
        return NoiseUtil.div(waterY + amount, worldHeight);
    }

    public float ground(int amount) {
        return NoiseUtil.div(groundY + amount, worldHeight);
    }

    public static float getSeaLevel(GeneratorSettings settings) {
        int worldHeight = Math.max(1, settings.world.worldHeight);
        int waterLevel = Math.min(settings.world.seaLevel, worldHeight);
        return NoiseUtil.div(waterLevel, worldHeight);
    }
}
