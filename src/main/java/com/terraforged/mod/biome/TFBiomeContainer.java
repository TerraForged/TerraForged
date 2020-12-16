/*
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

package com.terraforged.mod.biome;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.engine.util.pos.PosIterator;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.chunk.util.FastChunk;
import com.terraforged.noise.source.Line;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;

public class TFBiomeContainer extends BiomeContainer {

    private static final int BITS_WIDTH = (int) Math.round(Math.log(16.0D) / Math.log(2.0D)) - 2;
    private static final int ZOOM_VERT = (int) Math.round(Math.log(256.0D) / Math.log(2.0D)) - 2;
    public static final int BIOMES_3D_SIZE = 1 << BITS_WIDTH + BITS_WIDTH + ZOOM_VERT;
    public static final int BIOMES_2D_SIZE = 16 * 16;
    public static final int MASK_HORIZ = (1 << BITS_WIDTH) - 1;
    public static final int MASK_VERT = (1 << ZOOM_VERT) - 1;

    private final Biome[] biomes;
    private final Biome[] surface;
    private final Biome featureBiome;

    public TFBiomeContainer(Biome[] biomes, Biome[] surface, Biome feature, GameContext context) {
        super(context.biomes.getRegistry(), biomes);
        this.biomes = biomes;
        this.surface = surface;
        this.featureBiome = feature;
    }

    public Biome getBiome(int x, int z) {
        x &= 15;
        z &= 15;
        return surface[z * 16 + x];
    }

    public Biome getFeatureBiome() {
        return featureBiome;
    }

    public BiomeContainer bakeBiomes(boolean convertToVanilla, GameContext context) {
        if (convertToVanilla) {
            Biome[] biomeArray = new Biome[biomes.length];
            for (int i = 0; i < biomes.length; i++) {
                Biome biome = biomes[i];
                biome = ModBiomes.remap(biome, context);
                biomeArray[i] = biome;
            }
            return new BiomeContainer(context.biomes.getRegistry(), biomeArray);
        }
        return new BiomeContainer(context.biomes.getRegistry(), biomes);
    }

    public static TFBiomeContainer getOrCreate(IChunk chunk, ChunkReader reader, TerraBiomeProvider biomeProvider) {
        BiomeContainer biomes = chunk.getBiomes();
        if (biomes instanceof TFBiomeContainer) {
            return (TFBiomeContainer) biomes;
        }

        TFBiomeContainer container = TFBiomeContainer.create(reader, biomeProvider);
        if (chunk instanceof FastChunk) {
            ((FastChunk) chunk).setBiomes(container);
        } else {
            // replace/set the primer's biomes
            ((ChunkPrimer) chunk).setBiomes(container);
        }

        return container;
    }

    public static TFBiomeContainer create(ChunkReader chunkReader, TerraBiomeProvider biomeProvider) {
        Biome feature = null;
        float featureDist2 = Integer.MAX_VALUE;
        Biome[] biomes2D = new Biome[BIOMES_2D_SIZE];
        Biome[] biomes3D = new Biome[BIOMES_3D_SIZE];
        PosIterator iterator = PosIterator.area(0, 0, 16, 16);
        while (iterator.next()) {
            int dx = iterator.x();
            int dz = iterator.z();
            int x = chunkReader.getBlockX() + dx;
            int z = chunkReader.getBlockZ() + dz;
            Cell cell = chunkReader.getCell(dx, dz);
            Biome biome = biomeProvider.getBiome(cell, x, z);
            biomes2D[indexOf(dx, dz)] = biome;

            if (cell.biome.isExtreme()) {
                float dist2 = Line.dist2(dx, dz, 8, 8);
                if (feature == null || dist2 < featureDist2) {
                    featureDist2 = dist2;
                    feature = biome;
                }
            }

            if ((dx & 3) == 0 && (dz & 3) == 0) {
                for (int dy = 0; dy < 64; dy++) {
                    biomes3D[indexOf(dx >> 2, dy, dz >> 2)] = biome;
                }
            }
        }
        if (feature == null) {
            feature = biomes2D[indexOf(8, 8)];
        }
        return new TFBiomeContainer(biomes3D, biomes2D, feature, biomeProvider.getContext().gameContext);
    }

    private static int indexOf(int x, int z) {
        return (z << 4) + x;
    }

    public static int indexOf(int x, int y, int z) {
        x &= MASK_HORIZ;
        y = MathHelper.clamp(y, 0, MASK_VERT);
        z &= MASK_HORIZ;
        return y << BITS_WIDTH + BITS_WIDTH | z << BITS_WIDTH | x;
    }
}