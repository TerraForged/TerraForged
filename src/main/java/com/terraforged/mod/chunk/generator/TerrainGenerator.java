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

import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.column.BaseDecorator;
import com.terraforged.mod.chunk.util.TerraContainer;
import com.terraforged.mod.feature.TerrainHelper;
import com.terraforged.world.climate.Climate;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrains;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.StructureManager;

public class TerrainGenerator implements Generator.Terrain {

    private final Levels levels;
    private final Climate climate;
    private final Terrains terrain;
    private final TerrainHelper terrainHelper;
    private final TerraChunkGenerator generator;

    public TerrainGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
        this.levels = generator.getContext().levels;
        this.terrain = generator.getContext().terrain;
        this.climate = generator.getContext().factory.get().getClimate();
        this.terrainHelper = new TerrainHelper(0.75F, 4F);
    }

    @Override
    public final void generateTerrain(IWorld world, IChunk chunk, StructureManager structures) {
        try (ChunkReader reader = generator.getChunkReader(chunk.getPos().x, chunk.getPos().z)) {
            TerraContainer container = TerraContainer.getOrCreate(chunk, reader, generator.getBiomeProvider());
            try (DecoratorContext context = new DecoratorContext(chunk, levels, terrain, climate)) {
                reader.iterate(context, (cell, dx, dz, ctx) -> {
                    int px = ctx.blockX + dx;
                    int pz = ctx.blockZ + dz;
                    int py = ctx.levels.scale(cell.value);
                    ctx.cell = cell;
                    ctx.biome = container.getNoiseBiome(dx, world.getSeaLevel(), dz);
                    BaseDecorator.INSTANCE.decorate(ctx.chunk, ctx, px, py, pz);
                });

//                terrainHelper.flatten(world, chunk);
            }
        }
    }
}
