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

import com.terraforged.mod.data.ModTerrain;
import com.terraforged.mod.data.ModVegetation;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.biome.IBiomeSampler;
import com.terraforged.mod.worldgen.biome.feature.PositionSampler;
import com.terraforged.mod.worldgen.biome.viability.ViabilityContext;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.noise.util.N2DUtil;
import com.terraforged.noise.util.NoiseUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class ViabilityTest {
    public static final int SEED = 1234124;

    public static void main(String[] args) {
        int freq = 1;
        int width = 1200;
        int height = 800;

        float[] hsb = new float[3];

        var levels = TerrainLevels.DEFAULT;
        var noise = new NoiseGenerator(SEED, levels, ModTerrain.getTerrain(null));
        var heightmap = generate(width, height, freq, noise);

        var context = new ViabilityContext();
        context.biomeSampler = new IBiomeSampler.Sampler(noise);
        context.terrainData = CompletableFuture.completedFuture(new TerrainData(levels));

        var vegetations = ModVegetation.getVegetation(null);
        var vegetation = vegetations[2];

        var image = N2DUtil.render(width, height, (x, z, img) -> {
            var sample = heightmap.get(x, z);
            float scaledHeight = levels.getScaledHeight(sample.heightNoise);
            var biome = context.getClimateSampler().sample(x, z).cell.biome;

            int rgb;
            if (scaledHeight <= levels.seaLevel) {
                rgb = Color.HSBtoRGB(0.7F, 0.4F, 0.75F);
            } else {
                var c = biome.getColor();
                Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);

                float elev = (scaledHeight - levels.seaLevel) / (levels.genDepth - levels.seaLevel);
                elev = 0.25F + 0.75F * elev;

                rgb = Color.HSBtoRGB(hsb[0], hsb[1], elev * hsb[2]);
            }

            img.setRGB(x, z, rgb);
        });

        points(freq, image, levels, heightmap, vegetation, context);

        N2DUtil.display(width, height, (x, z, img) -> {
            return image.getRGB(x, z);
        }).setVisible(true);
    }

    private static void points(int freq,
                               BufferedImage image,
                               TerrainLevels levels,
                               Heightmap heightmap,
                               VegetationConfig vegetation,
                               ViabilityContext context) {
        float f = vegetation.frequency() * freq;
        float j = vegetation.jitter();

        int maxX = NoiseUtil.floor(image.getWidth() * freq);
        int maxZ = NoiseUtil.floor(image.getHeight() * freq);

        PositionSampler.sample(SEED, 0, 0, maxX, maxZ, f, j, context, (seed, offset, hash, x, z, ctx) -> {
            if (x < 0 || z < 0 || x >= heightmap.width || z >= heightmap.height) return false;

            var sample = heightmap.get(x, z);
            float scaledHeight = levels.getScaledHeight(sample.heightNoise);

            if (scaledHeight <= levels.seaLevel) return false;

            int px = x * freq;
            int pz = z * freq;
            context.getTerrain().getHeight().set(px, pz, scaledHeight);
            context.getTerrain().getTerrain().set(px, pz, sample.terrainType);
            context.getTerrain().getWater().set(px, pz, sample.riverNoise);
            context.getTerrain().getGradient().set(px, pz, 0F);

            float fit = vegetation.viability().getFitness(px, pz, context);
            if (fit * vegetation.density() < MathUtil.rand(hash)) return false;

            dot(x, z, image);

            return true;
        });
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

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                int px = (x - ox) * freq;
                int pz = (z - oz) * freq;
                heightmap.set(x, z, ForkJoinPool.commonPool().submit(() -> new NoiseSample(generator.getNoiseSample(px, pz))));
            }
        }

        return heightmap;
    }

    private static class Heightmap {
        private final int width, height;
        private final ForkJoinTask<NoiseSample>[] data;

        private Heightmap(int width, int height) {
            this.width = width;
            this.height = height;
            //noinspection unchecked
            this.data = new ForkJoinTask[width * height];
        }

        public void set(int x, int z, ForkJoinTask<NoiseSample> sample) {
            data[index(x, z)] = sample;
        }

        public NoiseSample get(int x, int z) {
            return data[index(x, z)].join();
        }

        private int index(int x, int z) {
            return z * width + x;
        }
    }
}
