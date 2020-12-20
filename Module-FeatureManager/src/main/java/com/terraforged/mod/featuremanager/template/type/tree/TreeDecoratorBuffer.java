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

package com.terraforged.mod.featuremanager.template.type.tree;

import com.terraforged.mod.featuremanager.template.decorator.BoundsRecorder;
import com.terraforged.mod.featuremanager.template.decorator.DecoratorBuffer;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.ISeedReader;

import java.util.*;

public class TreeDecoratorBuffer extends BoundsRecorder implements DecoratorBuffer {

    private static final List<BlockPos> EMPTY_LIST = Collections.emptyList();
    private static final Comparator<BlockPos> HIGHEST_FIRST = Comparator.comparingInt(Vector3i::getY);

    private List<BlockPos> logList = null;
    private List<BlockPos> leafList = null;

    public TreeDecoratorBuffer(ISeedReader delegate) {
        super(delegate);
    }

    @Override
    public ISeedReader getDelegate() {
        return super.getDelegate();
    }

    @Override
    public void setDelegate(ISeedReader world) {
        super.setDelegate(world);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags) {
        recordState(pos, state);
        return super.setBlockState(pos, state, flags);
    }

    @Override
    public void translate(BlockPos offset) {
        super.translate(offset);

        if (logList != null) {
            logList.replaceAll(pos -> pos.add(offset));
        }

        if (leafList != null) {
            leafList.replaceAll(pos -> pos.add(offset));
        }
    }

    public List<BlockPos> getLogs() {
        if (logList != null) {
            logList.sort(HIGHEST_FIRST);
            return logList;
        }
        return EMPTY_LIST;
    }

    public List<BlockPos> getLeaves() {
        if (leafList != null) {
            leafList.sort(HIGHEST_FIRST);
            return leafList;
        }
        return EMPTY_LIST;
    }

    private void recordState(BlockPos pos, BlockState state) {
        if (BlockTags.LEAVES.contains(state.getBlock())) {
            leafList = safeAdd(leafList, pos);
            return;
        }

        if (BlockTags.LOGS.contains(state.getBlock())) {
            logList = safeAdd(logList, pos);
        }
    }

    private static List<BlockPos> safeAdd(List<BlockPos> list, BlockPos pos) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(finalize(pos));
        return list;
    }

    private static BlockPos finalize(BlockPos pos) {
        if (pos instanceof BlockPos.Mutable) {
            return pos.toImmutable();
        }
        return pos;
    }
}
