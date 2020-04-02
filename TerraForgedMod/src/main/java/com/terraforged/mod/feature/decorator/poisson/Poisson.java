package com.terraforged.mod.feature.decorator.poisson;

import com.terraforged.core.util.concurrent.ObjectPool;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiConsumer;

public class Poisson {

    private static final int SAMPLES = 50;

    private final int radius;
    private final int radius2;
    private final float halfRadius;
    private final int maxDistance;
    private final int regionSize;
    private final int gridSize;
    private final float cellSize;
    private final ObjectPool<Vec2f[][]> pool;

    public Poisson(int radius) {
        int size = 48;
        this.radius = radius;
        this.radius2 = radius * radius;
        this.halfRadius = radius / 2F;
        this.maxDistance = radius * 2;
        this.regionSize = size - radius;
        this.cellSize = radius / NoiseUtil.SQRT2;
        this.gridSize = (int) Math.ceil(regionSize / cellSize);
        this.pool = new ObjectPool<>(3, () -> new Vec2f[gridSize][gridSize]);
    }

    public int getRadius() {
        return radius;
    }

    public void visit(int chunkX, int chunkZ, PoissonContext context, BiConsumer<Integer, Integer> consumer) {
        try (ObjectPool.Item<Vec2f[][]> grid = pool.get()) {
            clear(grid.getValue());
            context.startX = (chunkX << 4);
            context.startZ = (chunkZ << 4);
            context.endX = context.startX + 16;
            context.endZ = context.startZ + 16;
            int regionX = (context.startX >> 5);
            int regionZ = (context.startZ >> 5);
            context.offsetX = regionX << 5;
            context.offsetZ = regionZ << 5;
            context.random.setSeed(NoiseUtil.hash2D(context.seed, regionX, regionZ));
            int x = context.random.nextInt(regionSize);
            int z = context.random.nextInt(regionSize);
            visit(x, z, grid.getValue(), SAMPLES, context, consumer);
        }
    }

    private void visit(float px, float pz, Vec2f[][] grid, int samples, PoissonContext context, BiConsumer<Integer, Integer> consumer) {
        for (int i = 0; i < samples; i++) {
            float angle = context.random.nextFloat() * NoiseUtil.PI2;
            float distance = radius + (context.random.nextFloat() * maxDistance);
            float x = halfRadius + px + NoiseUtil.sin(angle) * distance;
            float z = halfRadius + pz + NoiseUtil.cos(angle) * distance;
            if (valid(x, z, grid, context)) {
                Vec2f vec = new Vec2f(x, z);
                visit(vec, context, consumer);
                int cellX = (int) (x / cellSize);
                int cellZ = (int) (z / cellSize);
                grid[cellZ][cellX] = vec;
                visit(vec.x, vec.y, grid, samples, context, consumer);
            }
        }
    }

    private void visit(Vec2f pos, PoissonContext context, BiConsumer<Integer, Integer> consumer) {
        int dx = context.offsetX + (int) pos.x;
        int dz = context.offsetZ + (int) pos.y;
        if (dx >= context.startX && dx < context.endX && dz >= context.startZ && dz < context.endZ) {
            consumer.accept(dx, dz);
        }
    }

    private boolean valid(float x, float z, Vec2f[][] grid, PoissonContext context) {
        if (x < 0 || x >= regionSize || z < 0 || z >= regionSize) {
            return false;
        }

        int cellX = (int) (x / cellSize);
        int cellZ = (int) (z / cellSize);
        if (grid[cellZ][cellX] != null) {
            return false;
        }

        float noise = context.density.getValue(context.offsetX + x, context.offsetZ + z);
        float radius2 = noise * this.radius2;

        int searchRadius = 2;
        int minX = Math.max(0, cellX - searchRadius);
        int maxX = Math.min(grid[0].length - 1, cellX + searchRadius);
        int minZ = Math.max(0, cellZ - searchRadius);
        int maxZ = Math.min(grid.length - 1, cellZ + searchRadius);

        for (int dz = minZ; dz <= maxZ; dz++) {
            for (int dx = minX; dx <= maxX; dx++) {
                Vec2f vec = grid[dz][dx];
                if (vec == null) {
                    continue;
                }

                float dist2 = vec.dist2(x, z);
                if (dist2 < radius2) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void clear(Vec2f[][] grid) {
        for (Vec2f[] row : grid) {
            Arrays.fill(row, null);
        }
    }

    public static void main(String[] args) {
        int size = 512;
        int radius = 6;

        int chunkSize = 16;
        int chunks = size / chunkSize;

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Poisson poisson = new Poisson(radius);
        PoissonContext context = new PoissonContext(213, new Random());
        context.density = Source.simplex(213, 100, 1).scale(2).bias(0);

        long time = 0L;
        long count = 0L;

        int chunkX = 342;
        int chunkZ = 546;
        for (int cz = 0; cz < chunks; cz++) {
            for (int cx = 0; cx < chunks; cx++) {
                long start = System.nanoTime();
                poisson.visit(chunkX + cx, chunkZ + cz, context, (x, z) -> {
                    x -= chunkX << 4;
                    z -= chunkZ << 4;
                    if (x < 0 || x >= image.getWidth() || z < 0 || z >= image.getHeight()) {
                        return;
                    }
                    image.setRGB(x, z, Color.WHITE.getRGB());
                });
                time += (System.nanoTime() - start);
                count++;
            }
        }

        double avg = (double) (time / count) / 1000000;
        System.out.println(avg + "ms");

        JFrame frame = new JFrame();
        frame.add(new JLabel(new ImageIcon(image)));
        frame.setVisible(true);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
