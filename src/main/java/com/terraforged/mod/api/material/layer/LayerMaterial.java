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

package com.terraforged.mod.api.material.layer;

import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;

public class LayerMaterial {

    public static final BlockState NONE = Blocks.AIR.defaultBlockState();

    private final int min;
    private final int max;
    private final BlockState fullState;
    private final BlockState layerState;
    private final Property<Integer> layerProperty;

    private LayerMaterial(BlockState fullState, BlockState layerState, Property<Integer> layerProperty) {
        this.layerProperty = layerProperty;
        this.min = min(layerProperty);
        this.max = max(layerProperty);
        this.layerState = layerState;
        this.fullState = fullState;
    }

    public Block getLayerType() {
        return layerState.getBlock();
    }

    public BlockState getFull() {
        return fullState;
    }

    public BlockState getState(float value) {
        return getState(getLevel(value));
    }

    public BlockState getState(int level) {
        if (level < min) {
            return LayerMaterial.NONE;
        }
        if (level >= max) {
            return fullState;
        }
        return layerState.setValue(layerProperty, level);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getLevel(float depth) {
        if (depth > 1) {
            depth = getDepth(depth);
        } else if (depth < 0) {
            depth = 0;
        }
        return NoiseUtil.round(depth * max);
    }

    public float getDepth(float height) {
        return height - (int) height;
    }

    private static int min(Property<Integer> property) {
        return property.getPossibleValues().stream().min(Integer::compareTo).orElse(0);
    }

    private static int max(Property<Integer> property) {
        return property.getPossibleValues().stream().max(Integer::compareTo).orElse(0);
    }

    public static LayerMaterial of(Block block) {
        return of(block, BlockStateProperties.LAYERS);
    }

    public static LayerMaterial of(Block block, Property<Integer> property) {
        return of(block.defaultBlockState(), block.defaultBlockState(), property);
    }

    public static LayerMaterial of(Block full, Block layer) {
        return of(full.defaultBlockState(), layer.defaultBlockState());
    }

    public static LayerMaterial of(Block full, Block layer, Property<Integer> property) {
        return of(full.defaultBlockState(), layer.defaultBlockState(), property);
    }

    public static LayerMaterial of(BlockState layer) {
        return of(layer, BlockStateProperties.LAYERS);
    }

    public static LayerMaterial of(BlockState layer, Property<Integer> property) {
        return of(layer.setValue(property, max(property)), layer);
    }

    public static LayerMaterial of(BlockState full, BlockState layer) {
        return of(full, layer, BlockStateProperties.LAYERS);
    }

    public static LayerMaterial of(BlockState full, BlockState layer, Property<Integer> property) {
        return new LayerMaterial(full, layer, property);
    }
}
