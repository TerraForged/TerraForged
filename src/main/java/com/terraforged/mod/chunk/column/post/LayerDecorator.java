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

package com.terraforged.mod.chunk.column.post;

import com.terraforged.mod.api.chunk.column.ColumnDecorator;
import com.terraforged.mod.api.chunk.column.DecoratorContext;
import com.terraforged.mod.api.material.layer.LayerManager;
import com.terraforged.mod.api.material.layer.LayerMaterial;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.task.LazySupplier;
import com.terraforged.engine.world.heightmap.Levels;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;

public class LayerDecorator implements ColumnDecorator {

    private final LazySupplier<LayerManager> layerManager;

    public LayerDecorator(LazySupplier<LayerManager> layerManager) {
        this.layerManager = layerManager;
    }

    @Override
    public void decorate(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        context.pos.setPos(x, y + 1, z);

        BlockState state = chunk.getBlockState(context.pos);
        if (state.isAir(chunk, context.pos)) {
            context.pos.setPos(x, y, z);
            state = chunk.getBlockState(context.pos);
            if (state.isAir(chunk, context.pos)) {
                return;
            }
        }

        LayerMaterial material = layerManager.get().getMaterial(state.getBlock());
        if (material == null) {
            return;
        }

        setLayer(chunk, context.pos, material, context.cell, context.levels, 0F);
    }

    private void setLayer(IChunk chunk, BlockPos pos, LayerMaterial material, Cell cell, Levels levels, float min) {
        float height = cell.value * levels.worldHeight;
        float depth = material.getDepth(height);
        if (depth > min) {
            int level = material.getLevel(depth);
            BlockState layer = material.getState(level);
            if (layer == LayerMaterial.NONE) {
                return;
            }
            chunk.setBlockState(pos, layer, false);
        }
    }
}
