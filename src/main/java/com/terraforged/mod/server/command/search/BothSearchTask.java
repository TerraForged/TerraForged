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

package com.terraforged.mod.server.command.search;

import net.minecraft.util.math.BlockPos;

public class BothSearchTask extends Search {

    private final int spacing;
    private final Search a;
    private final Search b;

    public BothSearchTask(BlockPos center, Search a, Search b) {
        super(center, Math.min(a.getMinRadius(), b.getMinRadius()));
        this.spacing = Math.min(a.getSpacing(), b.getSpacing());
        this.a = a;
        this.b = b;
    }

    @Override
    public int getSpacing() {
        return spacing;
    }

    @Override
    public boolean test(BlockPos pos) {
        return a.test(pos) && b.test(pos);
    }

    @Override
    public BlockPos success(BlockPos.Mutable pos) {
        return a.success(pos);
    }
}
