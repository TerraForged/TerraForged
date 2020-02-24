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

package com.terraforged.app.renderer;

import com.terraforged.app.Applet;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;

import java.awt.*;

public abstract class Renderer {

    protected final Applet applet;

    protected Renderer(Applet visualizer) {
        this.applet = visualizer;
    }

    public float getSurface(Cell<Terrain> cell, float height, int waterLevel, float el) {
        if (cell.tag == applet.getCache().getTerrain().volcanoPipe) {
            applet.fill(2, 80, 64);
            return height * 0.95F * el;
        }
        if (height < waterLevel) {
            float temp = cell.temperature;
            float tempDelta = temp > 0.5 ? temp - 0.5F : -(0.5F - temp);
            float tempAlpha = (tempDelta / 0.5F);
            float hueMod = 4 * tempAlpha;

            float depth = (waterLevel - height) /  (float) (90);
            float darkness = (1 - depth);
            float darknessMod = 0.5F + (darkness * 0.5F);

            applet.fill(60 - hueMod, 65, 90 * darknessMod);
            return height * el;
        } else if (applet.controller.getColorMode() == Applet.ELEVATION) {
            float hei = Math.min(1, Math.max(0, height - waterLevel) / (255F - waterLevel));
            float temp = cell.temperature;
            float moist = Math.min(temp, cell.moisture);

            float hue = 35 - (temp * (1 - moist)) * 25;
            float sat = 75 * (1 - hei);
            float bri = 50 + 40 * hei;
            applet.fill(hue, sat, bri);
            return height * el;
        } else if (applet.controller.getColorMode() == Applet.BIOME_TYPE) {
            Color c = cell.biomeType.getColor();
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            float bri = 90 + cell.biomeTypeMask * 10;
            applet.fill(hsb[0] * 100, hsb[1] * 100, hsb[2] * bri);
            return height * el;
        } else if (applet.controller.getColorMode() == Applet.BIOME) {
            float hue = applet.color(cell);
            float sat = 70;
            float bright = 50 + 50 * cell.riverMask;
            applet.fill(hue, sat, bright);
            return height * el;
        } else if(applet.controller.getColorMode() == Applet.TERRAIN_TYPE) {
            float hue = applet.color(cell);
            if (cell.tag == applet.getCache().getTerrain().coast) {
                hue = 15;
            }
            float modifier = cell.mask(0.4F, 0.5F, 0F, 1F);
            float modAlpha = 0.1F;
            float mod = (1 - modAlpha) + (modifier * modAlpha);
            float sat = 70;
            float bri = 70;
            applet.fill(hue, 65, 70);
            return height * el;
        } else if(applet.controller.getColorMode() == Applet.EROSION) {
            float change = cell.sediment + cell.erosion;
            float value = Math.abs(cell.sediment * 250);
            value = Math.max(0, Math.min(1, value));
            float hue = value * 70;
            float sat = 70;
            float bri = 70;
            applet.fill(hue, sat, bri);
            return height * el;
        } else {
            float hue = applet.color(cell);
            float sat = 70;
            float bri = 70;
            applet.fill(hue, sat, bri);
            return height * el;
        }
    }

    public abstract void render(float zoom);

    private void renderGradLine(int steps, float x1, float y1, float x2, float y2, float hue1, float hue2, float sat, float bright) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float fx = dx / steps;
        float fy = dy / steps;
        float dhue = hue2 - hue1;
        float fhue = dhue / steps;
        for (int i = 0; i < steps; i++) {
            float px1 = x1 + (i * fx);
            float py1 = y1 + (i * fy);
            float px2 = x2 + ((i + 1) * fx);
            float py2 = y2 + ((i + 1) * fy);
            float hue = (i + 1) * fhue;
            applet.stroke(hue1 + hue, sat, bright);
            applet.line(px1, py1, px2, py2);
        }
    }
}
