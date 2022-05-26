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

package com.terraforged.mod.worldgen.noise.continent.river;

import java.util.Arrays;

public class RiverPieces {
    public static final RiverPieces NONE = new RiverPieces();
    private static final int INITIAL_SIZE = RiverGenerator.DIRS.length;
    private static final int GROW_AMOUNT = 1;

    private int riverCount = 0;
    private int lakeCount = 0;
    private RiverNode[] riverNodes = new RiverNode[INITIAL_SIZE];
    private RiverNode[] lakeNodes = new RiverNode[INITIAL_SIZE];

    public RiverPieces reset() {
        riverCount = 0;
        lakeCount = 0;
        return this;
    }

    public int riverCount() {
        return riverCount;
    }

    public int lakeCount() {
        return lakeCount;
    }

    public RiverNode river(int i) {
        return riverNodes[i];
    }

    public RiverNode lake(int i) {
        return lakeNodes[i];
    }

    public void addRiver(RiverNode node) {
        riverNodes = ensureCapacity(riverCount, riverNodes);
        riverNodes[riverCount++] = node;
    }

    public void addLake(RiverNode node) {
        lakeNodes = ensureCapacity(lakeCount, lakeNodes);
        lakeNodes[lakeCount++] = node;
    }

    private RiverNode[] ensureCapacity(int size, RiverNode[] array) {
        if (size < array.length) return array;
        return Arrays.copyOf(array, size + GROW_AMOUNT);
    }
}
