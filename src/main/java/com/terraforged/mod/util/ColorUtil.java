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

package com.terraforged.mod.util;

import com.terraforged.mod.worldgen.noise.climate.ClimateSample;
import com.terraforged.mod.worldgen.noise.continent.ContinentPoints;
import com.terraforged.noise.util.NoiseUtil;

import java.awt.*;

public class ColorUtil {
    public static int shade(float brightness) {
        return Color.HSBtoRGB(0, 0, brightness);
    }

    public static int shade(Color color, float brightness) {
        return shade(color.getRed(), color.getGreen(), color.getBlue(), brightness);
    }

    public static int shade(int rgb, float brightness) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb >> 0) & 0xFF;
        return shade(r, g, b, brightness);
    }

    public static int shade(int r, int g, int b, float brightness) {
        r = NoiseUtil.floor(r * brightness);
        g = NoiseUtil.floor(g * brightness);
        b = NoiseUtil.floor(b * brightness);
        return ((0xFF << 24) | (r << 16) | (g << 8) | b);
    }

    public static int rgb(int r, int g, int b) {
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getBiomeColor(ClimateSample sample, float biomeNoiseStrength) {
        return getBiomeColor(sample, sample.biomeNoise, biomeNoiseStrength);
    }

    public static int getBiomeColor(ClimateSample sample, float shadeNoise, float shadeStrength) {
        return getColor(sample, NoiseUtil.lerp(1f, shadeNoise, shadeStrength));
    }

    public static int getColor(ClimateSample sample, float shade) {
        if (sample.continentNoise <= ContinentPoints.SHALLOW_OCEAN) return 0x0066DD;
        if (sample.continentNoise <= ContinentPoints.BEACH) return 0x0099DD;
        if (sample.riverNoise <= 0) return 0x0099DD;
        return ColorUtil.shade(sample.climateType.getColor(), shade);
    }
}
