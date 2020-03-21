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

package com.terraforged.mod.gui.preview;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.util.NoiseUtil;

import java.awt.*;

public enum RenderMode {
    BIOME_TYPE,
    TEMPERATURE,
    MOISTURE,
    BIOME_SHAPE,
    ;

    public Color color(Cell<Terrain> cell, GeneratorContext context) {
        float baseHeight = Levels.getSeaLevel(context.settings.generator);
        if (cell.value < baseHeight) {
            return new Color(40, 140, 200);
        }

        float bands = 10F;
        float alpha = 0.2F;
        float elevation = (cell.value - baseHeight) / (1F - baseHeight);

        int band = NoiseUtil.round(elevation * bands);
        float scale = 1F - alpha;
        float bias = alpha * (band / bands);

        float saturation = 0.7F;
        float brightness = 0.8F;

        switch (this) {
            case BIOME_SHAPE:
                return Color.getHSBColor(cell.biome, saturation, brightness);
            case BIOME_TYPE:
                Color color = cell.biomeType.getColor();
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
                return Color.getHSBColor(hsb[0], hsb[1], (hsb[2] * scale) + bias);
            case MOISTURE:
                return Color.getHSBColor(step(cell.moisture, 8) * 0.65F, saturation, brightness);
            case TEMPERATURE:
                return Color.getHSBColor(step(1 - cell.temperature, 8) * 0.65F, saturation, brightness);
            default:
                return Color.black;
        }
    }

    private static float step(float value, int steps) {
        return ((float) NoiseUtil.round(value * steps)) / steps;
    }

    public static int rgba(Color color) {
        return color.getRed() + (color.getGreen() << 8) + (color.getBlue() << 16) + (255 << 24);
    }
}
