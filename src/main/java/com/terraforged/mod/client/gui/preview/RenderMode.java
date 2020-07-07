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

package com.terraforged.mod.client.gui.preview;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.world.heightmap.Levels;

import java.awt.*;

public enum RenderMode {
    BIOME_TYPE {
        @Override
        public int getColor(Cell cell, float scale, float bias) {
            Color color = cell.biomeType.getColor();
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
            return rgba(hsb[0], hsb[1], (hsb[2] * scale) + bias);
        }
    },
    TRANSITION_POINTS {
        @Override
        public int getColor(Cell cell, float scale, float bias) {
            switch (cell.terrain.getType()) {
                case DEEP_OCEAN:
                    return rgba(0.65F, 0.7F, 0.7F);
                case SHALLOW_OCEAN:
                    return rgba(0.6F, 0.6F, 0.8F);
                case BEACH:
                    return rgba(0.2F, 0.4F, 0.75F);
                case COAST:
                    return rgba(0.35F, 0.75F, 0.65F);
                default:
                    if (cell.terrain.isRiver() || cell.terrain.isWetland()) {
                        return rgba(0.6F, 0.6F, 0.8F);
                    }
                    return rgba(0.3F, 0.7F, 0.5F);
            }
        }
    },
    TEMPERATURE {
        @Override
        public int getColor(Cell cell, float scale, float bias) {
            float saturation = 0.7F;
            float brightness = 0.8F;
            return rgba(step(1 - cell.temperature, 8) * 0.65F, saturation, brightness);
        }
    },
    MOISTURE {
        @Override
        public int getColor(Cell cell, float scale, float bias) {
            float saturation = 0.7F;
            float brightness = 0.8F;
            return rgba(step(cell.moisture, 8) * 0.65F, saturation, brightness);
        }
    },
    BIOME {
        @Override
        public int getColor(Cell cell, float scale, float bias) {
            float saturation = 0.7F;
            float brightness = 0.8F;
            return rgba(cell.biomeIdentity, saturation, brightness);
        }
    },
    MACRO_NOISE {
        @Override
        public int getColor(Cell cell, float scale, float bias) {
            float saturation = 0.7F;
            float brightness = 0.8F;
            return rgba(cell.macroNoise, saturation, brightness);
        }
    },
    TERRAIN_REGION {
        @Override
        public int getColor(Cell cell, float scale, float bias) {
            float saturation = 0.7F;
            float brightness = 0.8F;
            return rgba(cell.terrain.getHue(), saturation, brightness);
        }
    },
    ;

    public int getColor(Cell cell, Levels levels) {
        float baseHeight = levels.water;
        if (this != TRANSITION_POINTS) {
            if (cell.value < baseHeight) {
                return rgba(40, 140, 200);
            }
        }
        float bands = 10F;
        float alpha = 0.2F;
        float elevation = (cell.value - baseHeight) / (1F - baseHeight);
        int band = NoiseUtil.round(elevation * bands);
        float scale = 1F - alpha;
        float bias = alpha * (band / bands);
        return getColor(cell, scale, bias);
    }

    public abstract int getColor(Cell cell, float scale, float bias);

    private static float step(float value, int steps) {
        return ((float) NoiseUtil.round(value * steps)) / steps;
    }

    public static int rgba(float h, float s, float b) {
        int argb = Color.HSBtoRGB(h, s, b);
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue =  argb & 0xFF;
        return rgba(red, green, blue);
    }

    public static int rgba(int r, int g, int b) {
        return r + (g << 8) + (b << 16) + (255 << 24);
    }
}
