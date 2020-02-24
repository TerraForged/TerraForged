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

public class Mesh {

    private final Mesh inner;
    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;
    private final float quality;

    public Mesh(Mesh inner, float x0, float y0, float x1, float y1, float quality) {
        this.inner = inner;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.quality = quality;
    }

    public Mesh expand(float scale) {
        return expand(scale, scale);
    }

    public Mesh expand(float scale, float quality) {
        float width0 = getWidth();
        float height0 = getHeight();
        float width1 = width0 * scale;
        float height1 = height0 * scale;
        float deltaX = (width1 - width0) / 2F;
        float deltaY = (height1 - height0) / 2F;
        float newQuality = this.quality * quality;
        return new Mesh(this, x0 - deltaX, y0 - deltaY, x1 + deltaX, y1 + deltaY, newQuality);
    }

    public float getWidth() {
        return x1 - x0;
    }

    public float getHeight() {
        return y1 - y0;
    }

    public void start(float width, float height) {
        if (inner != null) {
            inner.start(width, height);
        }
    }

    public void render() {
        if (inner == null) {
            renderNormal();
        } else {
            renderCutout();
            inner.render();
        }
    }

    public void beginStrip() {
        if (inner != null) {
            inner.beginStrip();
        }
    }

    public void endStrip() {
        if (inner != null) {
            inner.endStrip();
        }
    }

    public void visit(float x, float y) {
        if (inner != null) {
            inner.visit(x, y);
        }
    }

    private void renderNormal() {
        beginStrip();
        iterate(x0, y0, x1, y1);
        endStrip();
    }

    private void renderCutout() {
        beginStrip();
        iterate(x0, y0, inner.x1, inner.y0);
        endStrip();

        beginStrip();
        iterate(inner.x1, y0, x1, inner.y1);
        endStrip();

        beginStrip();
        iterate(inner.x0, inner.y1, x1, y1);
        endStrip();

        beginStrip();
        iterate(x0, inner.y0, inner.x0, y1);
        endStrip();
    }

    private void iterate(float minX, float minY, float maxX, float maxY) {
        float x = minX - quality;
        float y = minY - quality;
        while (y < maxY) {
            y = Math.min(y + quality, maxY);
            beginStrip();
            while (x < maxX) {
                x = Math.min(x + quality, maxX);
                visit(x, y);
                visit(x, y + quality);
            }
            x = minX - quality;
            endStrip();
        }
    }
}
