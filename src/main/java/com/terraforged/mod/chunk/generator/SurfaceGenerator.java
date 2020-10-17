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

package com.terraforged.mod.chunk.generator;

import com.terraforged.api.biome.surface.ChunkSurfaceBuffer;
import com.terraforged.api.biome.surface.SurfaceContext;
import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.util.FastChunk;
import com.terraforged.mod.chunk.util.SimpleChunk;
import com.terraforged.mod.chunk.util.TerraContainer;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.INoiseGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.stream.IntStream;

public class SurfaceGenerator implements Generator.Surfaces {

    private final TerraChunkGenerator generator;
    private final INoiseGenerator surfaceNoise;

    public SurfaceGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
        this.surfaceNoise = new PerlinNoiseGenerator(new SharedSeedRandom(generator.getSeed()), IntStream.rangeClosed(-3, 0));
    }

    @Override
    public final void generateSurface(WorldGenRegion world, IChunk chunk) {
        try (ChunkReader reader = generator.getChunkReader(chunk.getPos().x, chunk.getPos().z)) {
            TerraContainer container = TerraContainer.getOrCreate(chunk, reader, generator.getBiomeProvider());
            ChunkSurfaceBuffer buffer = new ChunkSurfaceBuffer(SimpleChunk.wrap(chunk));

            try (SurfaceContext context = generator.getContext().surface(buffer, container, generator.getSettings())) {
                reader.iterate(context, (cell, dx, dz, ctx) -> {
                    int px = ctx.blockX + dx;
                    int pz = ctx.blockZ + dz;
                    int top = ctx.chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, dx, dz);

                    ctx.buffer.setSurfaceLevel(top);

                    ctx.cell = cell;
                    ctx.biome = ctx.biomes.getBiome(dx, dz);
                    ctx.noise = getSurfaceNoise(px, pz) * 15D;

                    if (ctx.biome == null) {
                        Log.err("Null biome at {},{}", px, pz);
                        return;
                    }

                    generator.getSurfaceManager().getSurface(ctx).buildSurface(px, pz, top, ctx);

                    int py = ctx.levels.scale(cell.value);

                    for (int i = 0; i < generator.getSurfaceDecorators().size(); i++) {
                        generator.getSurfaceDecorators().get(i).decorate(ctx.buffer, ctx, px, py, pz);
                    }
                });
                FastChunk.updateWGHeightmaps(chunk, context.pos);
            }
        }
    }

    private double getSurfaceNoise(int x, int z) {
        double scale = 0.0625D;
        double noiseX = x * scale;
        double noiseZ = z * scale;
        double unusedValue1 = scale;
        double unusedValue2 = (x & 15) * scale;
        return surfaceNoise.noiseAt(noiseX, noiseZ, unusedValue1, unusedValue2);
    }
}
