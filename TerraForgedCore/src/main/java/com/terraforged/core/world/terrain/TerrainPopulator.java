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

package com.terraforged.core.world.terrain;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.util.Seed;
import me.dags.noise.Module;
import me.dags.noise.Source;

import java.util.Arrays;
import java.util.function.BiFunction;

public class TerrainPopulator implements Populator {

    private final Terrain type;
    private final Module source;

    public TerrainPopulator(Module source, Terrain type) {
        this.type = type;
        this.source = source;
    }

    public Module getSource() {
        return source;
    }

    public Terrain getType() {
        return type;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float z) {
        cell.value = source.getValue(x, z);
        cell.tag = type;
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float y) {
        cell.tag = type;
    }

    public static TerrainPopulator[] combine(TerrainPopulator[] input, Seed seed, int scale) {
        return combine(input, (tp1, tp2) -> TerrainPopulator.combine(tp1, tp2, seed, scale));
    }

    public static TerrainPopulator combine(TerrainPopulator tp1, TerrainPopulator tp2, Seed seed, int scale) {
        Module combined = Source.perlin(seed.next(), scale, 1)
                .warp(seed.next(), scale / 2, 2, scale / 2)
                .blend(tp1.getSource(), tp2.getSource(), 0.5, 0.25);

        String name = tp1.getType().getName() + "-" + tp2.getType().getName();
        int id = Terrain.ID_START + 1 + tp1.getType().getId() * tp2.getType().getId();
        float weight = Math.min(tp1.getType().getWeight(), tp2.getType().getWeight());
        Terrain type = new Terrain(name, id, weight);

        return new TerrainPopulator(combined, type);
    }

    public static <T> T[] combine(T[] input, BiFunction<T, T, T> operator) {
        int length = input.length;
        for (int i = 1; i < input.length; i++) {
            length += (input.length - i);
        }

        T[] result = Arrays.copyOf(input, length);
        for (int i = 0, k = input.length; i < input.length; i++) {
            T t1 = input[i];
            result[i] = t1;
            for (int j = i + 1; j < input.length; j++, k++) {
                T t2 = input[j];
                T t3 = operator.apply(t1, t2);
                result[k] = t3;
            }
        }

        return result;
    }
}
