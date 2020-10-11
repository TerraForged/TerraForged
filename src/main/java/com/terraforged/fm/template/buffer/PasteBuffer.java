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

package com.terraforged.fm.template.buffer;

import com.terraforged.fm.template.PasteConfig;
import com.terraforged.fm.util.ObjectPool;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasteBuffer implements BufferIterator {

    private static final ObjectPool<PasteBuffer> pool = new ObjectPool<>(8, PasteBuffer::new);

    private boolean updatePostPlacement;
    private List<BlockPos> placedBlocks = Collections.emptyList();

    private int index = -1;
    private BlockPos pos = BlockPos.ZERO;

    @Override
    public int size() {
        return placedBlocks.size();
    }

    @Override
    public boolean next() {
        while (++index < placedBlocks.size()) {
            pos = placedBlocks.get(index);
            if (pos != BlockPos.ZERO) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    public PasteBuffer configure(PasteConfig config) {
        updatePostPlacement = config.checkBounds;
        placedBlocks.clear();
        index = -1;
        return this;
    }

    public void record(BlockPos position) {
        if (updatePostPlacement) {
            if (placedBlocks.isEmpty()) {
                placedBlocks = new ArrayList<>();
            }
            placedBlocks.add(position);
        }
    }

    public static ObjectPool.Item<PasteBuffer> retain(PasteConfig config) {
        ObjectPool.Item<PasteBuffer> buffer = pool.get();
        buffer.getValue().configure(config);
        return buffer;
    }
}
