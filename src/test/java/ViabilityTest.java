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

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.data.ModTerrains;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.biome.IBiomeSampler;
import com.terraforged.mod.worldgen.biome.decorator.PositionSampler;
import com.terraforged.mod.worldgen.biome.viability.ViabilityContext;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.noise.util.N2DUtil;
import com.terraforged.noise.util.NoiseUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class ViabilityTest {
    public static final int SEED = 1345;
    public static final Type TYPE = Type.HARDY;

    public static void main(String[] args) {
        var frame = new JFrame();
        frame.add(new JPanel() {
            {
                setPreferredSize(new Dimension(1000, 800));
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);

                var image = render(getWidth(), getHeight());

                g.drawImage(image, 0, 0, null);
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static BufferedImage render(int width, int height) {
        int freq = 20;

        float[] hsb = new float[3];

        var levels = new TerrainLevels();
        var noise = new NoiseGenerator(levels, ModTerrains.Factory.getDefault(null));
        var heightmap = generate(width, height, freq, noise).init(freq);

        var context = new ViabilityContext();
        context.biomeSampler = new IBiomeSampler.Sampler(noise);
        context.terrainData = CompletableFuture.completedFuture(new TerrainData(levels));

        var vegetations = TerraForged.VEGETATIONS.entries(null, VegetationConfig[]::new);
        var vegetation = vegetations[Type.TEMPERATE.ordinal()];

        var image = N2DUtil.render(width, height, (x, z, img) -> {
            var sample = heightmap.get(x, z);
            float scaledHeight = levels.getScaledHeight(sample.heightNoise);
            var biome = context.getClimateSampler().getSample(SEED, x, z).climateType;

            int rgb;
            if (scaledHeight <= levels.seaLevel) {
                rgb = Color.HSBtoRGB(0.7F, 0.4F, 0.75F);
            } else {
                var c = biome.getColor();
                Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);

                float elev = (scaledHeight - levels.seaLevel) / (levels.maxY - levels.seaLevel);
                elev = 0.25F + 0.75F * elev;

                float grad = NoiseUtil.clamp(heightmap.getGrad(x, z) * 400, 0, 1);
                grad = 0.5F + 0.5F * grad;

                rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] * elev * grad);
            }

            img.setRGB(x, z, rgb);
        });

        points(freq, image, levels, heightmap, vegetation, context);

        return image;
    }

    private static void points(int freq,
                               BufferedImage image,
                               TerrainLevels levels,
                               Heightmap heightmap,
                               VegetationConfig vegetation,
                               ViabilityContext context) {
        float f = 1;
        float fx = vegetation.frequency() * freq * f;
        float fz = vegetation.frequency() * freq * PositionSampler.SQUASH_FACTOR * f;
        float j = vegetation.jitter();

        int maxX = NoiseUtil.floor(image.getWidth() * freq);
        int maxZ = NoiseUtil.floor(image.getHeight() * freq * PositionSampler.SQUASH_FACTOR);

        PositionSampler.sample(SEED, 0, 0, 0, maxX, maxZ, fx, fz, j, context, (seed, offset, hash, x, z, ctx) -> {
            if (x < 0 || z < 0 || x >= heightmap.width || z >= heightmap.height) return 0;

            var sample = heightmap.get(x, z);
            float scaledHeight = levels.getScaledHeight(sample.heightNoise);
            if (scaledHeight <= levels.seaLevel) return 0;

            setup(x, z, freq, heightmap, context);

            int px = x * freq;
            int pz = z * freq;
            float noise = (1F - vegetation.density()) * MathUtil.rand(hash);
            float fit = vegetation.viability().getFitness(px, pz, context);
            if (fit < noise) return 0;

            dot(x, z, image);

            return 0;
        });
    }

    private static void setup(int x, int z, int freq, Heightmap heightmap, ViabilityContext context) {
        var sample = heightmap.get(x, z);
        float scaledHeight = context.getTerrain().getLevels().getScaledHeight(sample.heightNoise);
        int px = x * freq;
        int pz = z * freq;
        context.getTerrain().getHeight().set(px, pz, scaledHeight);
        context.getTerrain().getTerrain().set(px, pz, sample.terrainType);
        context.getTerrain().getRiver().set(px, pz, sample.riverNoise);
        context.getTerrain().getGradient().set(px, pz, heightmap.getGrad(x, z));
    }

    private static void dot(int x, int z, BufferedImage image) {
        int r = 0;
        float r2 = r * r;

        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                if (dx * dx + dz * dz > r2) continue;

                int ix = x + dx;
                int iz = z + dz;
                if (ix >= 0 && iz >= 0 && ix < image.getWidth() && iz < image.getHeight()) {
                    image.setRGB(ix, iz, 0xFFFFFF);
                }
            }
        }
    }

    private static Heightmap generate(int width, int height, int freq, NoiseGenerator generator) {
        var heightmap = new Heightmap(width, height);

        int ox = width >> 1;
        int oz = height >> 1;

        var tasks = new ArrayList<ForkJoinTask<?>>(width * height);
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                int px = (x - ox) * freq;
                int pz = (z - oz) * freq;

                final int index = heightmap.index(x, z);
                tasks.add(ForkJoinPool.commonPool().submit(() -> {
                    var sample = new NoiseSample(generator.getNoiseSample(SEED, px, pz));
                    heightmap.set(index, sample);
                    return null;
                }));
            }
        }

        ForkJoinTask.invokeAll(tasks);

        return heightmap;
    }

    private static class Heightmap {
        private final int width, height;
        private final NoiseSample[] data;
        private final float[] gradient;

        private Heightmap(int width, int height) {
            this.width = width;
            this.height = height;
            this.data = new NoiseSample[width * height];
            this.gradient = new float[width * height];
        }

        public Heightmap init(int scale) {
            int distance = 4;
            for (int z = 1; z < height - 2; z++) {
                for (int x = 1; x < width - 2; x++) {
                    var n = get(x + 0, z - 1);
                    var s = get(x + 0, z + 1);
                    var e = get(x + 1, z + 0);
                    var w = get(x - 1, z + 0);

                    float dx = e.heightNoise - w.heightNoise;
                    float dy = s.heightNoise - n.heightNoise;
                    float grad = NoiseUtil.sqrt(dx * dx + dy * dy);
                    gradient[index(x, z)] = NoiseUtil.clamp(grad, 0, 1);
                }
            }
            return this;
        }

        public void set(int index, NoiseSample sample) {
            data[index] = sample;
        }

        public NoiseSample get(int x, int z) {
            return data[index(x, z)];
        }

        public float getGrad(int x, int z) {
            return gradient[index(x, z)];
        }

        public int index(int x, int z) {
            return z * width + x;
        }
    }

    public enum Type {
        COPSE,
        HARDY,
        HARDY_SLOPES,
        SPARSE,
        RAINFOREST,
        SPARSE_RAINFOREST,
        TEMPERATE,
        PATCHY
    }
}
