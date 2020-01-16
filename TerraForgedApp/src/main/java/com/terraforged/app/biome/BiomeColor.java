package com.terraforged.app.biome;

import me.dags.noise.util.NoiseUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class BiomeColor {

    private static final BufferedImage image = load();
    private static final int width = image.getWidth() - 1;
    private static final int height = image.getHeight() - 1;

    public static int getRGB(float temp, float moist) {
        float humidity = temp * moist;
        temp = 1 - temp;
        humidity = 1 - humidity;
        int x = NoiseUtil.round(temp * width);
        int y = NoiseUtil.round(humidity * height);
        return image.getRGB(x, y);
    }


    private static BufferedImage load() {
        try (InputStream inputStream = BiomeColor.class.getResourceAsStream("/grass.png")) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }
}
