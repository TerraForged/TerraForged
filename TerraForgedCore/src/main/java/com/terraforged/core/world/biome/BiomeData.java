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

package com.terraforged.core.world.biome;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BiomeData implements Comparable<BiomeData> {

    private static float[] bounds = {0, 1 / 3F, 2 / 3F, 1F};

    public static final List<BiomeData> BIOMES = new ArrayList<>();
    public static BiomeData DEFAULT = new BiomeData("none", "", 1, 0.5F, 0.5F);

    public final String name;

    public final Object reference;
    public final float color;
    public final float rainfall;
    public final float temperature;

    public BiomeData(String name, Object reference, float color, float rainfall, float temperature) {
        this.reference = reference;
        this.name = name;
        this.rainfall = rainfall;
        this.temperature = temperature;
        this.color = color;
    }

    public BiomeData(String name, Object reference, int color, float rainfall, float temperature) {
        Color c = new Color(color);
        this.reference = reference;
        this.name = name;
        this.rainfall = rainfall;
        this.temperature = temperature;
        this.color = getHue(c.getRed(), c.getGreen(), c.getBlue());
    }

    @Override
    public int compareTo(BiomeData o) {
        return name.compareTo(o.name);
    }

    public static Collection<BiomeData> getBiomes(float temperature, float rainfall) {
        int temp = Math.min(3, (int) (bounds.length * temperature));
        int rain = Math.min(3, (int) (bounds.length * rainfall));
        return getBiomes(temp, rain);
    }

    public static Collection<BiomeData> getBiomes(int tempLower, int rainLower) {
        int temp0 = tempLower;
        int temp1 = temp0 + 1;
        int rain0 = rainLower;
        int rain1 = rain0 + 1;

        float tempMin = bounds[temp0];
        float tempMax = bounds[temp1];
        float rainMin = bounds[rain0];
        float rainMax = bounds[rain1];

        List<BiomeData> biomes = new ArrayList<>();
        for (BiomeData biome : BIOMES) {
            if (biome.temperature >= tempMin && biome.temperature <= tempMax
                    && biome.rainfall >= rainMin && biome.rainfall <= rainMax) {
                biomes.add(biome);
            }
        }

        if (biomes.isEmpty()) {
            biomes.add(DEFAULT);
        }

        return biomes;
    }

    public static Collection<BiomeData> getTempBiomes(float temperature) {
        int lower = Math.min(3, (int) (bounds.length * temperature));
        int upper = lower + 1;

        float min = bounds[lower];
        float max = bounds[upper];

        List<BiomeData> biomes = new ArrayList<>();
        for (BiomeData data : BIOMES) {
            if (data.temperature >= min && data.temperature <= max) {
                biomes.add(data);
            }
        }

        return biomes;
    }

    public static Collection<BiomeData> getRainBiomes(float rainfall) {
        int lower = Math.min(3, (int) (bounds.length * rainfall));
        int upper = lower + 1;

        float min = bounds[lower];
        float max = bounds[upper];

        List<BiomeData> biomes = new ArrayList<>();
        for (BiomeData data : BIOMES) {
            if (data.rainfall >= min && data.rainfall <= max) {
                biomes.add(data);
            }
        }

        return biomes;
    }

    private static float getHue(int red, int green, int blue) {
        float min = Math.min(Math.min(red, green), blue);
        float max = Math.max(Math.max(red, green), blue);

        if (min == max) {
            return 0;
        }

        float hue;
        if (max == red) {
            hue = (green - blue) / (max - min);

        } else if (max == green) {
            hue = 2f + (blue - red) / (max - min);

        } else {
            hue = 4f + (red - green) / (max - min);
        }

        hue = hue * 60;
        if (hue < 0) hue = hue + 360;

        return (Math.round(hue) / 360F) * 100F;
    }
}
