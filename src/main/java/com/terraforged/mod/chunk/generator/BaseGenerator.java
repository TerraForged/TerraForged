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

package com.terraforged.mod.chunk.generator;

import com.terraforged.engine.concurrent.task.LazySupplier;
import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.engine.world.WorldGeneratorFactory;
import com.terraforged.engine.world.climate.Climate;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.mod.Log;
import com.terraforged.mod.api.chunk.column.ColumnDecorator;
import com.terraforged.mod.api.chunk.column.DecoratorContext;
import com.terraforged.mod.biome.TFBiomeContainer;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.column.BaseDecorator;
import com.terraforged.mod.chunk.column.BaseGeoDecorator;
import com.terraforged.mod.chunk.column.BedrockDecorator;
import com.terraforged.mod.chunk.util.FastChunk;
import com.terraforged.mod.structure.StructureTerrain;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.StructureManager;

public class BaseGenerator implements Generator.Terrain {

    private final Levels levels;
    private final TFChunkGenerator generator;
    private final ColumnDecorator baseDecorator;
    private final ColumnDecorator bedrockDecorator;
    private final StructureTerrain structureTerrain;
    private final LazySupplier<Climate> climate;

    public BaseGenerator(TFChunkGenerator generator) {
        this.generator = generator;
        this.levels = generator.getContext().levels;
        this.climate = generator.getContext().worldGenerator.then(WorldGeneratorFactory::getClimate);
        this.structureTerrain = new StructureTerrain(0.8F, 4F);
        this.baseDecorator = getBaseDecorator(generator);
        this.bedrockDecorator = new BedrockDecorator(generator.getContext());
    }

    @Override
    public final void generateTerrain(IWorld world, IChunk chunk, StructureManager structures) {
        try (ChunkReader reader = generator.getChunkReader(chunk.getPos().x, chunk.getPos().z)) {
            TFBiomeContainer container = TFBiomeContainer.getOrCreate(FastChunk.wrap(chunk), reader, generator.getBiomeProvider());
            try (DecoratorContext context = new DecoratorContext(chunk, levels, climate.get())) {
                reader.iterate(context, (cell, dx, dz, ctx) -> {
                    int px = ctx.blockX + dx;
                    int pz = ctx.blockZ + dz;
                    int py = ctx.levels.scale(cell.value);
                    ctx.cell = cell;
                    ctx.biome = container.getBiome(dx, dz);
                    baseDecorator.decorate(ctx.chunk, ctx, px, py, pz);
                    bedrockDecorator.decorate(ctx.chunk, ctx, px, py, pz);
                });

                structureTerrain.apply(world, chunk);
            }
        }
    }

    private static ColumnDecorator getBaseDecorator(TFChunkGenerator generator) {
        if (generator.getContext().terraSettings.miscellaneous.strataDecorator) {
            Log.info("Loading strata base decorator");
            return new BaseGeoDecorator(generator);
        }
        Log.info("Loading vanilla base decorator");
        return BaseDecorator.INSTANCE;
    }
}
