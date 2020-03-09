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

import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.util.PosIterator;
import com.terraforged.core.world.terrain.Terrain;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;

// holds a 1:1 map of biomes in the chunk
// also holds the chunk's view on the heightmap for convenience
public class TerraContainer extends BiomeContainer {

    private static final int BITS_WIDTH = (int) Math.round(Math.log(16.0D) / Math.log(2.0D)) - 2;
    private static final int ZOOM_VERT = (int) Math.round(Math.log(256.0D) / Math.log(2.0D)) - 2;
    public static final int SIZE = 1 << BITS_WIDTH + BITS_WIDTH + ZOOM_VERT;
    public static final int MASK_HORIZ = (1 << BITS_WIDTH) - 1;
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
        x &= MASK_HORIZ;
        y = MathHelper.clamp(y, 0, MASK_VERT);
        z &= MASK_HORIZ;
        return y << BITS_WIDTH + BITS_WIDTH | z << BITS_WIDTH | x;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Biome[] biomes = new Biome[SIZE];
        private final Biome[] surfaceBiomeCache = new Biome[256];

        public void set(int x, int z, Biome biome) {
            surfaceBiomeCache[indexOf(x, z)] = biome;
        }

        public TerraContainer build(ChunkReader chunkReader) {
            // biome storage format is 1 biome pos == 4x4x4 blocks, stored in an 4x64x4 (xyz) array
            // sample the 1:1 surfaceBiomeCache every 4 blocks with a 2 block offset (roughly center of the 4x4 area)
            for (int dy = 0; dy < 64; dy++) {
                for (int dz = 0; dz < 4; dz++) {
                    for (int dx = 0; dx < 4; dx++) {
                        int x = dx * 4;
                        int z = dz * 4;
                        int index = indexOf(dx, dy, dz);
                        biomes[index] = surfaceBiomeCache[indexOf(x, z)];
                    }
                }
            }
            return new TerraContainer(this, chunkReader);
        }
    }
}
