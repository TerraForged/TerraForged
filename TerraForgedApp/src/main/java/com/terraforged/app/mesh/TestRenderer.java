package com.terraforged.app.mesh;

import com.terraforged.app.renderer.Renderer;
import com.terraforged.app.Applet;

public class TestRenderer extends Renderer {

    private static boolean printed = false;

    public TestRenderer(Applet visualizer) {
        super(visualizer);
    }

    @Override
    public void render(float zoom) {
        Mesh mesh = new NoiseMesh(applet, this, -64, -64, 64, 64);
        for (int i = 0; i < 1; i++) {
//            mesh = mesh.expand(4);
        }

        float width = mesh.getWidth();
        float height = mesh.getHeight();
        if (!printed) {
            printed = true;
            System.out.println(width + "x" + height);
        }

        applet.pushMatrix();
        mesh.start(mesh.getWidth(), mesh.getHeight());
        mesh.render();
        applet.popMatrix();
    }
}
