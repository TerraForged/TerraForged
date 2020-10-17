package com.terraforged.mod.feature.decorator.fastpoisson;

import com.terraforged.n2d.Source;
import net.minecraft.util.math.ChunkPos;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Viewer {

    public static void main(String[] args) {
        Random random = new Random(12345);
        FastPoisson poisson = new FastPoisson();
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        JLabel label = new JLabel(new ImageIcon(image));
        render(0, 0, image, poisson, random, label);
        JFrame frame = new JFrame();
        frame.add(label);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void render(int ox, int oz, BufferedImage image, FastPoisson poisson, Random random, JLabel label) {
        render(ox, oz, image, poisson, random);
        label.repaint();

        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            render(ox + 1, oz, image, poisson, random, label);
        });
    }

    private static void render(int ox, int oz, BufferedImage image, FastPoisson poisson, Random random) {
        // chunk grid
        for (int cz = 0; cz < image.getHeight(); cz ++) {
            for (int cx = 0; cx < image.getWidth(); cx ++) {
                image.setRGB(cx, cz, 0x000000);
            }
        }

        int offsetX = ox << 4;
        int offsetZ = oz << 4;
        FastPoissonContext config = new FastPoissonContext(6, 0.2F, Source.simplex(12, 140, 1).scale(1.4));

        // points
        for (int cz = 0; cz < image.getHeight(); cz += 16) {
            for (int cx = 0; cx < image.getWidth(); cx += 16) {
                int chunkX = (cx >> 4) + ox;
                int chunkZ = (cz >> 4) + oz;
                random.setSeed(ChunkPos.asLong(chunkX, chunkZ));

                poisson.visit(1, chunkX, chunkZ, random, config, image, (x, z, img) -> {
                    try {
                        img.setRGB(x - offsetX, z - offsetZ, 0xFFFFFF);
                    } catch (Throwable t) {
                        System.err.println(x + ":" + z + " -> " + t.getMessage());
                    }
                });
            }
        }
    }
}
