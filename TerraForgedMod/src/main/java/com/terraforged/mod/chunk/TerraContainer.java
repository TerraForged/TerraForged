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

package com.terraforged.mod.chunk;

import com.terraforged.api.biome.BiomeVariant;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.util.PosIterator;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.mod.util.Environment;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;

public class TerraContainer extends BiomeContainer {

    private static final int ZOOM_HORIZ = (int) Math.round(Math.log(16.0D) / Math.log(2.0D)) - 2;
    private static final int ZOOM_VERT = (int) Math.round(Math.log(256.0D) / Math.log(2.0D)) - 2;
    public static final int SIZE = 1 << ZOOM_HORIZ + ZOOM_HORIZ + ZOOM_VERT;
    public static final int MASK_HORIZ = (1 << ZOOM_HORIZ) - 1;
    public static final int MASK_VERT = (1 << ZOOM_VERT) - 1;

    private final Biome[] biomes;
    private final Biome[] surface;
    private final ChunkReader chunkReader;

    public TerraContainer(Builder builder, ChunkReader chunkReader) {
        super(builder.biomes);
        this.chunkReader = chunkReader;
        this.biomes = builder.biomes;
        this.surface = builder.surfaceBiomeCache;
    }

    public Biome getBiome(int x, int z) {
        return surface[indexOf(x, z)];
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        return super.getNoiseBiome(x, y, z);
    }

    public Biome getFeatureBiome() {
        PosIterator iterator = PosIterator.area(0, 0, 16, 16);
        while (iterator.next()) {
            Cell<Terrain> cell = chunkReader.getCell(iterator.x(), iterator.z());
            if (cell.biomeType.isExtreme()) {
                return getBiome(iterator.x(), iterator.z());
            }
        }
        return getBiome(8, 8);
    }

    public BiomeContainer bakeBiomes() {
        if (Environment.isDev()) {
            for (int i = 0; i < biomes.length; i++) {
                Biome biome = biomes[i];
                if (biome instanceof BiomeVariant) {
                    biomes[i] = ((BiomeVariant) biome).getBase();
                }
            }
        }
        return new BiomeContainer(biomes);
    }

    public ChunkReader getChunkReader() {
        return chunkReader;
    }

    private static int indexOf(int x, int z) {
        x &= 15;
        z &= 15;
        return (z << 4) + x;
    }

    private static int indexOf(int x, int y, int z) {
        int bx = x & MASK_HORIZ;
        int by = MathHelper.clamp(y, 0, MASK_VERT);
        int bz = z & MASK_HORIZ;
        return by << ZOOM_HORIZ + ZOOM_HORIZ | bz << ZOOM_HORIZ | bx;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Biome[] biomes = new Biome[SIZE];
        private final Biome[] surfaceBiomeCache = new Biome[256];

        public void set(int x, int y, int z, Biome biome) {
            biomes[indexOf(x, y, z)] = biome;

            surfaceBiomeCache[indexOf(x, z)] = biome;
        }

        public void fill(int x, int z, Biome biome) {
            for (int y = 0; y < 64; y++) {
                set(x, y, z, biome);
            }
        }

        public TerraContainer build(ChunkReader chunkReader) {
            return new TerraContainer(this, chunkReader);
        }
    }
}
