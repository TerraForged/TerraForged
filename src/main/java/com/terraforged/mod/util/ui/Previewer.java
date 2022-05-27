/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.util.ui;

import com.terraforged.mod.worldgen.noise.continent.ContinentPreview;
import com.terraforged.noise.Module;
import com.terraforged.noise.util.NoiseUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class Previewer extends JPanel {
    private final Supplier<Shader> shaderSupplier;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private Shader shader;
    private int lastX = 0;
    private int lastY = 0;

    private float posX, posY;
    private float zoom = 1f;

    public Previewer(Supplier<Shader> shaderSupplier) {
        this.shaderSupplier = shaderSupplier;
        this.shader = shaderSupplier.get();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                posX += (lastX - e.getX()) * zoom;
                posY += (lastY - e.getY()) * zoom;
                lastX = e.getX();
                lastY = e.getY();
                redraw();
            }
        });

        this.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom = Math.max(0.0001f, zoom + e.getWheelRotation() * 0.75f);
                redraw();
            }
        });

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == 'r') {
                    shader = Previewer.this.shaderSupplier.get();
                    redraw();
                }
                if (e.getKeyChar() == 't') {
                    zoom = 1.0f;
                    redraw();
                }
                if (e.getKeyChar() == 's') {
                    ContinentPreview.SEED = ThreadLocalRandom.current().nextInt();
                    shader = Previewer.this.shaderSupplier.get();
                    redraw();
                }
            }
        });

        setFocusable(true);
        setPreferredSize(new Dimension(400, 400));
    }

    protected void redraw() {
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        int w = getWidth();
        int h = getHeight();

        float ox = posX - (w >> 1) * zoom;
        float oy = posY - (h >> 1) * zoom;

        var image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        each(ox, oy, zoom, 0, 0, w, h, shader, image).join();

        g.drawImage(image, 0, 0, w, h, null);


        int width = NoiseUtil.floor(w * zoom);
        int height = NoiseUtil.floor(h * zoom);

        g.setColor(Color.WHITE);
        g.drawString(String.format("%sx%s", width, height), 2, 12);
    }

    public static void display(Supplier<Module> supplier) {
        launch(() -> {
            final var noise = supplier.get();
            return (x, y) -> Color.HSBtoRGB(0, 0, noise.getValue(x, y));
        });
    }

    public static void launch(Supplier<Shader> shader) {
        var frame = new JFrame();
        frame.add(new Previewer(shader));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private interface Visitor<T> {
        void visit(int x, int y, float value, T ctx);
    }

    public interface Shader {
        int getRGB(float x, float y);
    }

    private <T> CompletableFuture<Void> each(float ox, float oy, float zoom, int x0, int y0, int x1, int y1, Shader shader, BufferedImage image) {
        int tileDivs = (int) Math.floor(Math.sqrt(Runtime.getRuntime().availableProcessors()));
        int tileSizeX = Worker.getTileSize(x1 - x0, tileDivs);
        int tileSizeY = Worker.getTileSize(y1 - y0, tileDivs);

        var tasks = new CompletableFuture[tileDivs * tileDivs];
        for (int ty = 0; ty < tileDivs; ty++) {
            int minY = y0 + ty * tileSizeY;
            int maxY = Math.min(minY + tileSizeY, y1);

            for (int tx = 0; tx < tileDivs; tx++) {
                int minX = x0 + tx * tileSizeX;
                int maxX = Math.min(minX + tileSizeX, x1);
                var worker = new Worker<>(ox, oy, zoom, minX, minY, maxX, maxY, shader, image);
                tasks[ty * tileDivs + tx] = CompletableFuture.runAsync(worker, executor);
            }
        }

        return CompletableFuture.allOf(tasks);
    }

    private static class Worker<T> implements Runnable {
        private final float ox, oy, zoom;

        private final int minX, minY;
        private final int maxX, maxY;
        private final Shader shader;
        private final BufferedImage image;

        private Worker(float ox, float oy, float zoom, int minX, int minY, int maxX, int maxY, Shader shader, BufferedImage image) {
            this.ox = ox;
            this.oy = oy;
            this.zoom = zoom;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.shader = shader;
            this.image = image;
        }

        @Override
        public void run() {
            for (int y = minY; y < maxY; y++) {
                float py = oy + y * zoom;

                for (int x = minX; x < maxX; x++) {
                    float px = ox + x * zoom;
                    int rgb = shader.getRGB(px, py);
                    image.setRGB(x, y, rgb);
                }
            }
        }

        protected static int getTileSize(int dimension, int divs) {
            int size = dimension / divs;
            if (size * divs < dimension) {
                size++;
            }
            return size;
        }
    }
}
