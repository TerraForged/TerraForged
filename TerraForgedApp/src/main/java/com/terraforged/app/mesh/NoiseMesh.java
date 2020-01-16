package com.terraforged.app.mesh;

import com.terraforged.app.renderer.Renderer;
import com.terraforged.app.Applet;
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
