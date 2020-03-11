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

package com.terraforged.api.material.state;

import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CachedState extends StateSupplier {

    private static final List<CachedState> all = new ArrayList<>();

    private final Supplier<BlockState> supplier;

    private volatile BlockState reference = null;

    public CachedState(Supplier<BlockState> supplier) {
        this.supplier = supplier;
        all.add(this);
    }

    @Override
    public BlockState get() {
        BlockState state = reference;
        if (state == null) {
            state = supplier.get();
            reference = state;
        }
        return state;
    }

    public CachedState clear() {
        reference = null;
        return this;
    }

    public static void clearAll() {
        all.forEach(CachedState::clear);
    }
}
