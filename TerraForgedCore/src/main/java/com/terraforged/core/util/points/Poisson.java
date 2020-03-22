package com.terraforged.core.util.points;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.Region;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.Vec2i;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Poisson {

    public static void main(String[] args) {
        Region region = new Region(0, 0, 5, 2);
        BufferedImage image = new BufferedImage(region.getBlockSize().size, region.getBlockSize().size, BufferedImage.TYPE_INT_RGB);
        points(region, 123,8, 20F, 0.8F);
        render(region, image);

        JFrame frame = new JFrame();
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void points(Region region, int seed, int scale, float radius, float threshold) {
        region.generate(chunk -> chunk.generate((cell, dx, dz) -> {}));

        Module noise = Source.simplex(seed, scale, 1);

        int gridSize = (int) Math.ceil(region.getBlockSize().size / radius);
        Vec2i[][] grid = new Vec2i[gridSize][gridSize];

        for (int dz = 0; dz < region.getBlockSize().size; dz++) {
            for (int dx = 0; dx < region.getBlockSize().size; dx++) {
                int x = region.getBlockX() + dx;
                int z = region.getBlockZ() + dz;
                float value = noise.getValue(x, z);
                region.getCell(dx, dz).value = value;
                if (true) continue;

                if (value > threshold) {
                    continue;
                }

                int gridX = (int) (dx / radius);
                int gridZ = (int) (dz / radius);
                Vec2i point = grid[gridZ][gridX];

                if (point != null) {
                    Cell<?> current = region.getCell(point.x, point.y);
                    if (current.value > value) {
                        continue;
                    }
                }

                region.getCell(dx, dz).value = value;
                grid[gridZ][gridX] = new Vec2i(dx, dz);
            }
        }
    }

    private static void render(Region region, BufferedImage image) {
        region.iterate((cell, dx, dz) -> {
            if (cell.value == 0) {
                return;
            }
            int w = (int) (cell.value * 255);
            image.setRGB(dx, dz, new Color(w, w, w).getRGB());
        });
    }
}
