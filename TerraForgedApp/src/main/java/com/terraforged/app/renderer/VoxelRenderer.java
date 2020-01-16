package com.terraforged.app.renderer;

import com.terraforged.app.Applet;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import processing.core.PApplet;

public class VoxelRenderer extends Renderer {

    public VoxelRenderer(Applet visualizer) {
        super(visualizer);
    }

    @Override
    public void render(float zoom) {
        Levels levels = new Levels(applet.getCache().getSettings().generator);
        int worldHeight = applet.getCache().getSettings().generator.world.worldHeight;
        int waterLevel = levels.waterY;
        int resolution = applet.getCache().getRegion().getBlockSize().size;
        int center = resolution / 2;

        float w = applet.width / (float) resolution;
        float h = applet.width / (float) resolution;
        float el = w / zoom;

        applet.pushMatrix();
        applet.translate(-applet.width / 2F, -applet.width / 2F);

        int difX = Math.max(0, applet.getCache().getRegion().getBlockSize().size - resolution);
        int difY = Math.max(0, applet.getCache().getRegion().getBlockSize().size - resolution);
        int offsetX = difX / 2;
        int offsetZ = difY / 2;

        for (int dy = 0; dy < resolution; dy++) {
            for (int dx = 0; dx < resolution; dx++) {
                Cell<Terrain> cell = applet.getCache().getRegion().getCell(dx + offsetX, dy + offsetZ);

                float cellHeight = cell.value * worldHeight;
                int height = Math.min(worldHeight, Math.max(0, (int) cellHeight));

                if (height < 0) {
                    continue;
                }

                float x0 = dx * w;
                float x1 = (dx + 1) * w;
                float z0 = dy * h;
                float z1 = (dy + 1) * h;
                float y = getSurface(cell, height, waterLevel, el);

                if ((dx == center && (dy == center || dy - 1 == center || dy + 1 == center))
                        || (dy == center && (dx - 1 == center || dx + 1 == center))) {
                    applet.fill(100F, 100F, 100F);
                }

                drawColumn(x0, z0, 0, x1, z1, y);
            }
        }

        drawRulers(16, worldHeight, resolution, h, el);

        applet.popMatrix();
    }

    private void drawRulers(int step, int max, int resolution, float unit, float height) {
        float width = (resolution + 1) * unit;
        int doubleStep = step * 2;

        for (int dz = 0; dz <= 1; dz++) {
            for (int dx = 0; dx <= 1; dx++) {
                float x0 = dx * width;
                float x1 = x0 - unit;
                float z0 = dz * width;
                float z1 = z0 - unit;
                for (int dy = 0; dy < max; dy += step) {
                    float y0 = dy * height;
                    float y1 = y0 + (step * height);
                    float h = 100, s = 100, b = 100;
                    if ((dy % doubleStep) != step) {
                        s = 0;
                    }
                    applet.fill(h, s, b);
                    drawColumn(x0, z0, y0, x1, z1, y1);
                }
            }
        }
    }

    private void drawColumn(float x0, float y0, float z0, float x1, float y1, float z1) {
        applet.beginShape(PApplet.QUADS);
        // +Z "front" face
        applet.vertex(x0, y0, z1);
        applet.vertex(x1, y0, z1);
        applet.vertex(x1, y1, z1);
        applet.vertex(x0, y1, z1);

        // -Z "back" face
        applet.vertex(x1, y0, z0);
        applet.vertex(x0, y0, z0);
        applet.vertex(x0, y1, z0);
        applet.vertex(x1, y1, z0);

        // +Y "bottom" face
        applet.vertex(x0, y1, z1);
        applet.vertex(x1, y1, z1);
        applet.vertex(x1, y1, z0);
        applet.vertex(x0, y1, z0);

        // -Y "top" face
        applet.vertex(x0, y0, z0);
        applet.vertex(x1, y0, z0);
        applet.vertex(x1, y0, z1);
        applet.vertex(x0, y0, z1);

        // +X "right" face
        applet.vertex(x1, y0, z1);
        applet.vertex(x1, y0, z0);
        applet.vertex(x1, y1, z0);
        applet.vertex(x1, y1, z1);

        // -X "left" face
        applet.vertex(x0, y0, z0);
        applet.vertex(x0, y0, z1);
        applet.vertex(x0, y1, z1);
        applet.vertex(x0, y1, z0);

        applet.endShape();
    }
}
