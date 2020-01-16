package com.terraforged.app;

import com.terraforged.app.renderer.MeshRenderer;
import com.terraforged.app.renderer.Renderer;
import com.terraforged.app.renderer.VoxelRenderer;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class Applet extends PApplet {

    public static final int ELEVATION = 1;
    public static final int BIOME_TYPE = 2;
    public static final int TEMPERATURE = 3;
    public static final int MOISTURE = 4;
    public static final int BIOME = 5;
    public static final int STEEPNESS = 6;
    public static final int TERRAIN_TYPE = 7;
    public static final int EROSION = 8;
    public static final int CONTINENT = 9;

    protected Renderer mesh = new MeshRenderer(this);
    protected Renderer voxel = new VoxelRenderer(this);

    public final Controller controller = new Controller();

    public abstract Cache getCache();

    public abstract float color(Cell<Terrain> cell);

    @Override
    public void settings() {
        size(800, 800, P3D);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        controller.mousePress(event);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        controller.mouseRelease(event);
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        controller.mouseDrag(event);
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        controller.mouseWheel(event);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        controller.keyPress(event);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        controller.keyRelease(event);
    }

    public void leftAlignText(int margin, int top, int lineHeight, String... lines) {
        noLights();
        fill(0, 0, 100);
        for (int i = 0; i < lines.length; i++) {
            int y = top + (i * lineHeight);
            text(lines[i], margin, y);
        }
    }


    public void drawTerrain(float zoom) {
        if (controller.getRenderMode() == 0) {
            voxel.render(zoom);
        } else {
            mesh.render(zoom);
        }
    }

    public String colorModeName() {
        switch (controller.getColorMode()) {
            case STEEPNESS:
                return "GRADIENT";
            case TEMPERATURE:
                return "TEMPERATURE";
            case MOISTURE:
                return "MOISTURE";
            case TERRAIN_TYPE:
                return "TERRAIN TYPE";
            case ELEVATION:
                return "ELEVATION";
            case BIOME_TYPE:
                return "BIOME TYPE";
            case BIOME:
                return "BIOME";
            case EROSION:
                return "EROSION";
            case CONTINENT:
                return "CONTINENT";
            default:
                return "-";
        }
    }

    public static float hue(float value, int steps, int max) {
        value = Math.round(value * (steps - 1));
        value /= (steps - 1);
        return value * max;
    }

    public void drawCompass() {
        pushStyle();
        pushMatrix();
        textSize(200);
        fill(100, 0, 100);

        char[] chars = {'N', 'E', 'S', 'W'};
        for (int r = 0; r < 4; r++) {
            char c = chars[r];
            float x = -textWidth(c) / 2;
            float y = -width * 1.2F;
            text(c, x, y);
            rotateZ(0.5F * PI);
        }

        popMatrix();
        popStyle();
        textSize(16);
    }
}
