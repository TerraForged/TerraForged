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

package com.terraforged.app.mesh;

import com.terraforged.app.Applet;
import com.terraforged.app.renderer.Renderer;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;
import processing.core.PApplet;

public class NoiseMesh extends Mesh {

    private final Applet applet;
    private final Renderer renderer;
    private final Cell<Terrain> cell = new Cell<>();

    public NoiseMesh(Applet applet, Renderer renderer, float x0, float y0, float x1, float y1) {
        super(null, x0, y0, x1, y1, 1F);
        this.applet = applet;
        this.renderer = renderer;
    }

    @Override
    public void start(float width, float height) {
        applet.noStroke();
    }

    @Override
    public void beginStrip() {
        applet.beginShape(PApplet.TRIANGLE_STRIP);
    }

    @Override
    public void endStrip() {
        applet.endShape();
    }

    @Override
    public void visit(float x, float y) {
        float height = cell.value * 255;
        float surface = renderer.getSurface(cell, height, 63, 10F);
        applet.vertex(x * 10F, y * 10F, (int) surface);
    }
}
