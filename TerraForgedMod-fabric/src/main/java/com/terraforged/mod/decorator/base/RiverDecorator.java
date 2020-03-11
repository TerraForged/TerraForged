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

package com.terraforged.mod.decorator.base;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.api.material.state.States;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.mod.chunk.TerraContext;
import me.dags.noise.Module;
import me.dags.noise.Source;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.Chunk;

public class RiverDecorator implements ColumnDecorator {

    private final Levels levels;
    private final Terrain river;
    private final Terrain riverBank;

    private final BlockState dirt;
    private final BlockState sand;
    private final BlockState gravel;

    private final Module noise1;

    public RiverDecorator(TerraContext context) {
        Seed seed = context.seed.nextSeed();
        this.levels = context.levels;
        this.river = context.terrain.river;
        this.riverBank = context.terrain.riverBanks;
        this.dirt = States.DIRT.get();
        this.sand = States.SAND.get();
        this.gravel = States.GRAVEL.get();
        this.noise1 = Source.perlin(seed.next(), 50, 1);
    }

    @Override
    public void decorate(Chunk chunk, DecoratorContext context, int x, int y, int z) {
        if (context.cell.tag == river) {
            chunk.setBlockState(context.pos.set(x, y, z), dirt, false);
            return;
        }
        if (context.cell.tag == riverBank) {
            float value = noise1.getValue(x, z) * 5;
            if (y + value >= levels.waterY) {
                if (context.cell.steepness > 0.5) {
                    chunk.setBlockState(context.pos.set(x, y, z), gravel, false);
                } else if (context.cell.steepness < 0.3) {
                    chunk.setBlockState(context.pos.set(x, y, z), sand, false);
                } else {
                    chunk.setBlockState(context.pos.set(x, y, z), dirt, false);
                }
            } else {
                chunk.setBlockState(context.pos.set(x, y, z), dirt, false);
            }
        }
    }
}
