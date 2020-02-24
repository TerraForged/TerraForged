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
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import processing.core.PApplet;

public class MeshRenderer extends Renderer {

    public MeshRenderer(Applet visualizer) {
        super(visualizer);
    }

    @Override
    public void render(float zoom) {
        float seaLevel = new Levels(applet.getCache().getSettings().generator).water;
        int worldHeight = applet.getCache().getSettings().generator.world.worldHeight;
        int waterLevel = (int) (seaLevel * worldHeight);
        int seabedLevel = (int) ((seaLevel - 0.04) * worldHeight);
        int resolution = applet.getCache().getRegion().getBlockSize().size;

        float w = applet.width / (float) (resolution - 1);
        float h = applet.width / (float) (resolution - 1);

        applet.noStroke();
        applet.pushMatrix();
        applet.translate(-applet.width / 2F, -applet.width / 2F);

        for (int dy = 0; dy < resolution - 1; dy++) {
            applet.beginShape(PApplet.TRIANGLE_STRIP);
            for (int dx = 0; dx < resolution; dx++) {
                draw(dx, dy, w, h, zoom, worldHeight, waterLevel, seabedLevel);
                draw(dx, dy + 1, w, h, zoom, worldHeight, waterLevel, resolution / 2);
            }
            applet.endShape();
        }

        applet.popMatrix();
    }

    private void draw(int dx, int dz, float w, float h, float zoom, int worldHeight, int waterLevel, int center) {
        Cell<Terrain> cell = applet.getCache().getRegion().getCell(dx, dz);
        float height = (cell.value * worldHeight);
        float x = dx * w;
        float z = dz * h;
        float y = (int) getSurface(cell, height, waterLevel, 1);
        applet.vertex(x, z, y / (zoom * 0.2F));
    }
}
