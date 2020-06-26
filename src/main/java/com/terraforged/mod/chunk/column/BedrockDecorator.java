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

package com.terraforged.mod.chunk.column;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.api.material.state.States;
import com.terraforged.mod.chunk.TerraContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.ThreadLocalRandom;

public class BedrockDecorator implements ColumnDecorator {

    private final int minDepth;
    private final int variance;
    private final BlockState material;

    public BedrockDecorator(TerraContext context) {
        minDepth = context.terraSettings.dimensions.bedrockLayer.minDepth;
        variance = context.terraSettings.dimensions.bedrockLayer.variance;
        material = getState(context.terraSettings.dimensions.bedrockLayer.material);
    }

    @Override
    public void decorate(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        if (variance <= 0) {
            fillDown(context, chunk, x, z, minDepth - 1, -1, material);
        } else {
            fillDown(context, chunk, x, z, minDepth + ThreadLocalRandom.current().nextInt(variance), -1, material);
        }
    }

    private static BlockState getState(String name) {
        ResourceLocation location = ResourceLocation.tryCreate(name);
        if (location != null && ForgeRegistries.BLOCKS.containsKey(location)) {
            Block block = ForgeRegistries.BLOCKS.getValue(location);
            if (block != null) {
                return block.getDefaultState();
            }
        }
        return States.BEDROCK.get();
    }
}
