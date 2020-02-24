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

package com.terraforged.core.util;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.Module;
import me.dags.noise.Source;

import java.util.function.BiPredicate;

public class VariablePredicate {

    private final Module module;
    private final BiPredicate<Cell<Terrain>, Float> predicate;

    public VariablePredicate(Module module, BiPredicate<Cell<Terrain>, Float> predicate) {
        this.module = module;
        this.predicate = predicate;
    }

    public boolean test(Cell<Terrain> cell, float x, float z) {
        return predicate.test(cell, module.getValue(x, z));
    }

    public static VariablePredicate height(Seed seed, Levels levels, int min, int max, int size, int octaves) {
        float bias = levels.scale(min);
        float scale = levels.scale(max - min);
        Module source = Source.perlin(seed.next(), size, 1).scale(scale).bias(bias);
        return new VariablePredicate(source, (cell, height) -> cell.value < height);
    }
}
