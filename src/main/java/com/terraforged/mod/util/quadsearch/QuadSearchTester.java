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

package com.terraforged.mod.util.quadsearch;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QuadSearchTester {

    public static void main(String[] args) {
        final int step = 16;
        final int size = 512;
        final int half = size >> 1;

        Search<int[]> search = find(size, step);
        SearchContext context = new SearchContext(10, TimeUnit.SECONDS);
        int[] grid = QuadSearch.asyncSearch(half, half, half, step, 8, search, context);

        if (grid == null) {
            System.out.println("no result");
            grid = search.result();
        }

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                int rgb = grid[x + z * size];
                img.setRGB(x, z, rgb);
            }
        }

        JFrame frame = new JFrame();
        frame.add(new JLabel(new ImageIcon(img.getScaledInstance(size, size, BufferedImage.SCALE_DEFAULT))));
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void printColors(int[] grid) {
        Map<Integer, AtomicInteger> counts = new LinkedHashMap<>();
        for (int i : grid) {
            if (i == 0) {
                continue;
            }
            counts.computeIfAbsent(i, k -> new AtomicInteger()).incrementAndGet();
        }
        counts.forEach((color, count) -> System.out.println(Integer.toHexString(color) + ":" + count));
    }

    private static Search<int[]> find(int size, int step) {
        final Random random = new Random(12);

        final int rx = Math.max(step, random.nextInt(size));
        final int rz = Math.max(step, random.nextInt(size));
        final int findX = Math.floorDiv(rx, step) * step;
        final int findZ = Math.floorDiv(rz, step) * step;

        return new Search<int[]>() {

            private int[] grid = new int[size * size];

            @Override
            public int[] result() {
                int[] result = grid;
                grid = null;
                return result;
            }

            @Override
            public boolean test(int x, int z) {
                int testX = Math.floorDiv(x, step) * step;
                int testZ = Math.floorDiv(z, step) * step;

                if (testX == findX && testZ == findZ) {
                    set(x, z, grid, size, step, 0xFFFFFF);
                    return false;
                } else {
                    int color = Thread.currentThread().hashCode() & 0xFFFFFF;
                    set(x, z, grid, size, step, color);
                    return false;
                }
            }
        };
    }

    private static void set(int x, int z, int[] grid, int size, int step, int color) {
        if (grid == null) {
            return;
        }

        int halfStep = Math.max(0, (step >> 1) - Math.max(1, step >> 2));
        double radius2 = Math.max(2, (halfStep + 0.49) * (halfStep + 0.49));

        for (int dy = -halfStep; dy <= halfStep; dy++) {
            for (int dx = -halfStep; dx <= halfStep; dx++) {
                if (dx * dx + dy * dy < radius2) {
                    set(x + dx, z + dy, grid, size, color);
                }
            }
        }
    }

    private static void set(int x, int z, int[] grid, int size, int color) {
        if (x < 0 || x >= size || z < 0 || z >= size) return;
        int index = x + z * size;
        if (grid[index] != 0) System.out.println(x + ":" + z);
        grid[index] = color;
    }
}
