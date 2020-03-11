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

package com.terraforged.mod.decorator.feature;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.decorator.base.ErosionDecorator;
import me.dags.noise.source.Rand;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;

import java.util.function.Predicate;

public class SnowEroder extends ErosionDecorator {

    private static final float SNOW_ROCK_STEEPNESS = 0.45F;
    private static final float SNOW_ROCK_HEIGHT = 95F / 255F;

    private final int seed1;
    private final int seed2;
    private final int seed3;
    private final Rand rand;

    public SnowEroder(TerraContext context) {
        super(context);
        this.seed1 = context.seed.next();
        this.seed2 = context.seed.next();
        this.seed3 = context.seed.next();
        this.rand = context.factory.getHeightmap().getClimate().getRand();
    }

    @Override
    public void decorate(Chunk chunk, DecoratorContext context, int x, int y, int z) {
        int surface = chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y - surface > 0) {
            if (y - surface > 4) {
                return;
            }
            y = surface;
        }

        if (context.biome.getTemperature(context.pos.set(x, y, z)) <= 0.25) {
            float var = -ColumnDecorator.getNoise(x, z, seed1, 16, 0);
            float hNoise = rand.getValue(x, z, seed2) * HEIGHT_MODIFIER;
            float sNoise = rand.getValue(x, z, seed3) * SLOPE_MODIFIER;
            float vModifier = context.cell.tag == context.terrains.volcano ? 0.15F : 0F;
            float height = context.cell.value + var + hNoise + vModifier;
            float steepness = context.cell.steepness + var + sNoise + vModifier;
            if (snowErosion(x, z, steepness, height)) {
                Predicate<BlockState> predicate = Heightmap.Type.MOTION_BLOCKING.getBlockPredicate();
                for (int dy = 2; dy > 0; dy--) {
                    context.pos.setY(y + dy);
                    BlockState state = chunk.getBlockState(context.pos);
                    if (!predicate.test(state) || state.getBlock() == Blocks.SNOW) {
                        chunk.setBlockState(context.pos, Blocks.AIR.getDefaultState(), false);
                    }
                }
            }
        }
    }

    private boolean snowErosion(float x, float z, float steepness, float height) {
        return steepness > ROCK_STEEPNESS
                || (steepness > SNOW_ROCK_STEEPNESS && height > SNOW_ROCK_HEIGHT)
                || super.erodeDirt(x, z, steepness, height);
    }
}
