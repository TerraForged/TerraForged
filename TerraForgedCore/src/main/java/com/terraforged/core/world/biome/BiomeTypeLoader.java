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

package com.terraforged.core.world.biome;

import me.dags.noise.util.NoiseUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BiomeTypeLoader {

    private static BiomeTypeLoader instance;

    private final float[][] edges = new float[BiomeType.RESOLUTION][BiomeType.RESOLUTION];
    private final BiomeType[][] map = new BiomeType[BiomeType.RESOLUTION][BiomeType.RESOLUTION];

    public BiomeTypeLoader() {
        generateTypeMap();
        generateEdgeMap();
    }

    public BiomeType[][] getTypeMap() {
        return map;
    }

    public float[][] getEdgeMap() {
        return edges;
    }

    private BiomeType getType(int x, int y) {
        return map[y][x];
    }

    private void generateTypeMap() {
        try {
            BufferedImage image = ImageIO.read(BiomeType.class.getResourceAsStream("/biomes.png"));
            float xf = image.getWidth() / (float) BiomeType.RESOLUTION;
            float yf = image.getHeight() / (float) BiomeType.RESOLUTION;
            for (int y = 0; y < BiomeType.RESOLUTION; y++) {
                for (int x = 0; x < BiomeType.RESOLUTION; x++) {
                    if (BiomeType.MAX - y > x) {
                        map[BiomeType.MAX - y][x] = BiomeType.ALPINE;
                        continue;
                    }
                    int ix = NoiseUtil.round(x * xf);
                    int iy = NoiseUtil.round(y * yf);
                    int argb = image.getRGB(ix, iy);
                    Color color = fromARGB(argb);
                    map[BiomeType.MAX - y][x] = forColor(color);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateEdgeMap() {
        int[] distances = new int[BiomeType.values().length];
        for (int y = 0; y < BiomeType.RESOLUTION; y++) {
            for (int x = 0; x < BiomeType.RESOLUTION; x++) {
                if (y > x) continue;
                BiomeType type = getType(x, y);
                if (type == BiomeType.ALPINE) {
                    continue;
                }
                int distance2 = getEdge(x, y, type);
                edges[y][x] = distance2;
                distances[type.ordinal()] = Math.max(distances[type.ordinal()], distance2);
            }
        }

        for (int y = 0; y < BiomeType.RESOLUTION; y++) {
            for (int x = 0; x < BiomeType.RESOLUTION; x++) {
                BiomeType type = getType(x, y);
                int max = distances[type.ordinal()];
                float distance = edges[y][x];
                float value = NoiseUtil.pow(distance / max, 0.33F);
                edges[y][x] = NoiseUtil.clamp(value, 0, 1);
            }
        }
    }

    private int getEdge(int cx, int cy, BiomeType type) {
        int radius = BiomeType.RESOLUTION / 4;
        int distance2 = Integer.MAX_VALUE;
        int x0 = Math.max(0, cx - radius);
        int x1 = Math.min(BiomeType.MAX, cx + radius);
        int y0 = Math.max(0, cy - radius);
        int y1 = Math.min(BiomeType.MAX, cy + radius);
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                BiomeType neighbour = getType(x, y);

                if (neighbour == BiomeType.ALPINE) {
                    continue;
                }

                if (neighbour != type) {
                    int dist2 = dist2(cx, cy, x, y);
                    if (dist2 < distance2) {
                        distance2 = dist2;
                    }
                }
            }
        }
        return distance2;
    }

    private static BiomeType forColor(Color color) {
        BiomeType type = null;
        int closest = Integer.MAX_VALUE;
        for (BiomeType t : BiomeType.values()) {
            int distance2 = getDistance2(color, t.getLookup());
            if (distance2 < closest) {
                closest = distance2;
                type = t;
            }
        }
        if (type == null) {
            return BiomeType.GRASSLAND;
        }
        return type;
    }

    private static int getDistance2(Color a, Color b) {
        int dr = a.getRed() - b.getRed();
        int dg = a.getGreen() - b.getGreen();
        int db = a.getBlue() - b.getBlue();
        return dr * dr + dg * dg + db * db;
    }

    private static Color fromARGB(int argb) {
        int b = (argb) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        return new Color(r, g, b);
    }

    private static int dist2(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    private static BufferedImage generateEdgeMapImage() {
        BufferedImage image = new BufferedImage(BiomeType.RESOLUTION, BiomeType.RESOLUTION, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < BiomeType.RESOLUTION; y++) {
            for (int x = 0; x < BiomeType.RESOLUTION; x++) {
                float temperature = x / (float) BiomeType.RESOLUTION;
                float moisture = y / (float) BiomeType.RESOLUTION;
                float value = BiomeType.getEdge(temperature, moisture);
                int color = Color.HSBtoRGB(0, 0, value);
                image.setRGB(x, image.getHeight() - 1 - y, color);
            }
        }
        return image;
    }

    public static BiomeTypeLoader getInstance() {
        if (instance == null) {
            instance = new BiomeTypeLoader();
        }
        return instance;
    }

    public static void main(String[] args) throws Throwable {
        BufferedImage img = generateEdgeMapImage();
        ImageIO.write(img, "png", new File("biomes_dist.png"));
        JLabel label = new JLabel(new ImageIcon(img));
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(label);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
