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

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.fix.RegionFix;
import com.terraforged.mod.chunk.util.TerraContainer;
import com.terraforged.mod.util.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;

import java.util.List;

public class FeatureGenerator implements Generator.Features {

    private final TerraChunkGenerator generator;

    public FeatureGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public final void generateFeatures(WorldGenRegion region, StructureManager manager) {
        int chunkX = region.getMainChunkX();
        int chunkZ = region.getMainChunkZ();
        IChunk chunk = region.getChunk(chunkX, chunkZ);

        ChunkReader reader = generator.getChunkReader(chunkX, chunkZ);
        TerraContainer container = TerraContainer.getOrCreate(chunk, reader, generator.getBiomeProvider());

        Biome biome = container.getFeatureBiome(reader);
        DecoratorContext context = generator.getContext().decorator(chunk);

        ISeedReader regionFix = new RegionFix(region, generator);
        BlockPos pos = new BlockPos(context.blockX, 0, context.blockZ);

        // place biome features
        generator.getFeatureManager().decorate(generator, manager, regionFix, chunk, biome, pos);

        // run post processes on chunk
        postProcess(reader, container, context);

        // bake biome array
        ((ChunkPrimer) chunk).setBiomes(container.bakeBiomes(Environment.isVanillaBiomes(), generator.getContext().gameContext));

        // close the current chunk reader
        reader.close();

        // mark chunk disposed as this is the last usage of the reader
        reader.dispose();
    }

    private void postProcess(ChunkReader reader, TerraContainer container, DecoratorContext context) {
        List<ColumnDecorator> decorators = generator.getPostProcessors();
        reader.iterate(context, (cell, dx, dz, ctx) -> {
            int px = ctx.blockX + dx;
            int pz = ctx.blockZ + dz;
            int py = ctx.chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, dx, dz);
            ctx.cell = cell;
            ctx.biome = container.getBiome(dx, dz);
            for (ColumnDecorator decorator : decorators) {
                decorator.decorate(ctx.chunk, ctx, px, py, pz);
            }
        });
    }
}
