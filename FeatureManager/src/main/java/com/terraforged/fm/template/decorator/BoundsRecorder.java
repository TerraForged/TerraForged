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

package com.terraforged.fm.template.decorator;

import com.terraforged.fm.util.delegate.SeedWorldDelegate;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BoundsRecorder extends SeedWorldDelegate {

    private BlockPos.Mutable min = null;
    private BlockPos.Mutable max = null;
    private Set<BlockPos> allPositions = null;

    public BoundsRecorder(ISeedReader delegate) {
        super(delegate);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        recordPos(pos);
        recordMin(pos.getX(), pos.getY(), pos.getZ());
        recordMax(pos.getX(), pos.getY(), pos.getZ());
        return super.setBlockState(pos, newState, flags);
    }

    public void translate(BlockPos offset) {
        allPositions = getAllPositions().stream()
                .map(pos -> pos.add(offset))
                .collect(Collectors.toSet());
    }

    public Set<BlockPos> getAllPositions() {
        if (allPositions == null) {
            return Collections.emptySet();
        }
        return allPositions;
    }

    public MutableBoundingBox getBounds() {
        if (min == null || max == null) {
            return MutableBoundingBox.getNewBoundingBox();
        }
        return MutableBoundingBox.createProper(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    private void recordPos(BlockPos pos) {
        if (allPositions == null) {
            allPositions = new HashSet<>();
        }
        allPositions.add(pos);
    }

    private void recordMin(int x, int y, int z) {
        if (min == null) {
            min = new BlockPos.Mutable(x, y, z);
        } else {
            x = Math.min(x, min.getX());
            y = Math.min(y, min.getY());
            z = Math.min(z, min.getZ());
            min.setPos(x, y, z);
        }
    }

    private void recordMax(int x, int y, int z) {
        if (max == null) {
            max = new BlockPos.Mutable(x, y, z);
        } else {
            x = Math.max(x, max.getX());
            y = Math.max(y, max.getY());
            z = Math.max(z, max.getZ());
            max.setPos(x, y, z);
        }
    }
}
