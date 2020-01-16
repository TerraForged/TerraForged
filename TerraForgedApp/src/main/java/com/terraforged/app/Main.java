package com.terraforged.app;

import com.terraforged.app.biome.BiomeProvider;
import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.biome.BiomeData;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.core.world.terrain.Terrain;

import java.awt.*;
import java.util.Random;

public class Main extends Applet {

    public static void main(String[] args) {
        Main.start(-1);
    }

    public static void start(long seed) {
        Main.seed = (int) seed;
        Main.random = seed == -1;
        main(Main.class.getName());
    }

    private static boolean random = false;
    public static int seed = -1;

    private Cache cache;
    private float offsetX = 0;
    private float offsetZ = 0;
    private final String bits = System.getProperty("sun.arch.data.model");
    private final BiomeProvider biomeProvider = new BiomeProvider();

    @Override
    public Cache getCache() {
        return cache;
    }

    @Override
    public void setup() {
        super.setup();
        if (random) {
            setSeed(new Random(System.currentTimeMillis()).nextInt());
            offsetX = 0;
            offsetZ = 0;
        }
        setSeed(seed);
    }

    public void setSeed(int seed) {
        Main.seed = seed;
        cache = new Cache(seed);
        System.out.println(seed);
    }

    @Override
    public float color(Cell<Terrain> cell) {
        switch (controller.getColorMode()) {
            case STEEPNESS:
                return hue(1 - cell.steepness, 64, 70);
            case TEMPERATURE:
                return hue(1 - cell.temperature, 64, 70);
            case MOISTURE:
                return hue(cell.moisture, 64, 70);
            case TERRAIN_TYPE:
                if (cell.tag == getCache().getTerrain().volcano) {
                    return 0F;
                }
                return 20 + (cell.tag.getId() / (float) Terrain.MAX_ID.get()) * 80;
            case ELEVATION:
                float value = (cell.value - 0.245F) / 0.65F;
                return (1 - value) * 30;
            case BIOME:
                BiomeData biome = biomeProvider.getBiome(cell);
                if (biome == null) {
                    return 0F;
                }
                return cell.biome * 70;
            case CONTINENT:
                return cell.continent * 70;
            default:
                return 50;
        }
    }

    @Override
    public void draw() {
        int nextSeed = controller.getNewSeed();
        if (nextSeed == 1) {
            setup();
        } else if (nextSeed != 0) {
            setSeed(nextSeed);
        }

        offsetX += controller.velocityX() * controller.zoomLevel() * controller.zoomLevel();
        offsetZ += controller.velocityY() * controller.zoomLevel() * controller.zoomLevel();
        cache.update(offsetX, offsetZ, controller.zoomLevel(), controller.filters());

        // color stuff
        noStroke();
        background(0);
        colorMode(HSB, 100);

        // lighting
        ambientLight(0, 0, 75, width / 2, -height, height / 2);
        pointLight(0, 0, 50, width / 2, -height * 100, height / 2);

        // render
        pushMatrix();
        controller.apply(this);
//        translate(-width / 2F, -height / 2F);
        drawTerrain(controller.zoomLevel());
        drawCompass();
//        translate(0, 0, 255 * (width / (float) controller.resolution()) / controller.zoomLevel());
//        mesh.renderWind(controller.resolution(), controller.zoomLevel());
        popMatrix();

        pushMatrix();
        translate(0, 0, -1);
//        drawGradient(0, height - 150, 100F, width, 150);
        popMatrix();

        drawStats();
        drawBiomeKey();
        drawControls();
    }

    private void drawGradient(int x, int y, float d, float w, float h) {
        noFill();
        for (int dy = 0; dy <= h; dy++) {
            float dist = Math.min(1, dy / d);
            stroke(0, 0, 0, dist * 100F);
            line(x, y + dy, x + w, y + dy);
        }
        noStroke();
    }

    private void drawStats() {
        int resolution = cache.getRegion().getBlockSize().size;
        int blocks = NoiseUtil.round(resolution * controller.zoomLevel());

        String[][] info = {
                {"Java:", String.format("x%s", bits)},
                {"Fps: ", String.format("%.3f", frameRate)},
                {"Seed:", String.format("%s", seed)},
                {"Zoom: ", String.format("%.2f", controller.zoomLevel())},
                {"Area: ", String.format("%sx%s [%sx%s]", blocks, blocks, resolution, resolution)},
                {"Center: ", String.format("x=%.0f, y=%s, z=%.0f", offsetX, cache.getCenterHeight(), offsetZ)},
                {"Terrain: ", String.format("%s:%s", cache.getCenterTerrain().getName(),
                        cache.getCenterBiomeType().name())},
                {"Biome: ", String.format("%s", biomeProvider.getBiome(cache.getCenterCell()).name)},
                {"Overlay: ", colorModeName()},
        };

        int widest = 0;
        for (String[] s : info) {
            widest = Math.max(widest, (int) textWidth(s[0]));
        }

        int top = 20;
        int lineHeight = 15;
        for (String[] s : info) {
            leftAlignText(10, top, 0, s[0]);
            top += lineHeight;
        }

        top = 20;
        for (String[] s : info) {
            if (s.length == 2) {
                leftAlignText(12 + widest, top, 0, s[1]);
                top += lineHeight;
            }
        }
    }

    private void drawBiomeKey() {
        int top = 20;
        int lineHeight = 15;
        int widest = 0;
        for (BiomeType type : BiomeType.values()) {
            widest = Math.max(widest, (int) textWidth(type.name()));
        }

        int left = width - widest - lineHeight - 15;
        for (BiomeType type : BiomeType.values()) {
            leftAlignText(left, top, 0, type.name());
            float[] hsb = Color.RGBtoHSB(type.getColor().getRed(), type.getColor().getGreen(),
                    type.getColor().getBlue(), null);
            fill(0, 0, 100);
            rect(width - lineHeight - 11, top - lineHeight + 1, lineHeight + 2, lineHeight + 2);

            fill(hsb[0] * 100, hsb[1] * 100, hsb[2] * 100);
            rect(width - lineHeight - 10, top - lineHeight + 2, lineHeight, lineHeight);
            top += lineHeight;
        }
    }

    private void drawControls() {
        String[][][] columns = {
                {
                        {"Mouse-Left + Move", " - Rotate terrain"},
                        {"Mouse-Right + Move", " - Pan terrain"},
                        {"Mouse-Scroll + Move", " - Zoom camera"},
                        {"Mouse-Scroll + LCTRL + Move", " - Zoom terrain"},
                        {"WASD", "- Move terrain"}
                }, {
                {"Key 1-8", "- Select overlay"},
                {"Key R", "- Toggle mesh renderer"},
                {"Key F", "- Toggle filters"},
                {"Key N", "- Generate new world"},
                {"Key C", "- Copy seed to clipboard"},
                {"Key V", "- Paste seed from clipboard"},
        }};

        int lineHeight = 15;
        int rows = 0;
        for (String[][] column : columns) {
            rows = Math.max(rows, column.length);
        }

        int left = 10;
        int widest = 0;
        for (String[][] column : columns) {
            int top = (height - 10) - ((rows - 1) * lineHeight);

            for (String[] row : column) {
                int width = 0;
                for (String cell : row) {
                    leftAlignText(left + width, top, 0, cell);
                    width += (int) textWidth(cell);
                }
                top += lineHeight;
                widest = Math.max(widest, width);
            }

            left += widest + 10;
        }
    }
}
