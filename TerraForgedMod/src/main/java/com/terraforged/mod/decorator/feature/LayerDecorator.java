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
import com.terraforged.api.material.layer.LayerManager;
import com.terraforged.api.material.layer.LayerMaterial;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.mod.material.MaterialHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;

public class LayerDecorator implements ColumnDecorator {

    private final LayerManager layerManager;

    public LayerDecorator(LayerManager layerManager) {
        this.layerManager = layerManager;
    }

    @Override
    public void decorate(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        context.pos.setPos(x, y + 1, z);

        // if block is already a layer-type then simply set the layer property
        BlockState state = chunk.getBlockState(context.pos);
        LayerMaterial material = layerManager.getMaterial(state.getBlock());
        if (material != null) {
            setLayer(chunk, context.pos, material, context.cell, context.levels, 0F);
            return;
        }

        if (MaterialHelper.isAir(state.getBlock())) {
            return;
        }

        // block is non-solid (grass/flower etc)
        if (!state.getMaterial().blocksMovement()) {
            // block below is solid
            if (chunk.getBlockState(context.pos.setPos(x, y, z)).getMaterial().blocksMovement()) {
                // block above is air
                if (MaterialHelper.isAir(chunk.getBlockState(context.pos.setPos(x, y + 2, z)).getBlock())) {
//                    setLayer(chunk, pos.setPos(x, y + 1, z), context.cell, context.levels, 0.25F);
                }
            }
        }
    }

    private void setLayer(IChunk chunk, BlockPos pos, LayerMaterial material, Cell<?> cell, Levels levels, float min) {
        float height = cell.value * levels.worldHeight;
        float depth = material.getDepth(height);
        if (depth > min) {
            int level = material.getLevel(depth);
            BlockState layer = material.getState(level);
            if (MaterialHelper.isAir(layer.getBlock())) {
                return;
            }
            chunk.setBlockState(pos, layer, false);
        }
    }
}
