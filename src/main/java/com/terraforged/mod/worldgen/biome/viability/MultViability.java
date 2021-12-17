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

package com.terraforged.mod.worldgen.biome.viability;

import com.terraforged.cereal.spec.DataSpec;

import java.util.List;

import static com.terraforged.mod.worldgen.biome.viability.SumViability.getRules;

public record MultViability(Viability... rules) implements Viability {
    public static final DataSpec<MultViability> SPEC = DataSpec.builder(
            "Multiply",
            MultViability.class,
            (data, spec, context) -> new MultViability(spec.get("rules", data, v -> getRules(v, context))))
            .addList("rules", MultViability::getRulesList).build();

    @Override
    public float getFitness(int x, int z, Context context) {
        float value = 1F;

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < rules.length; i++) {
            value *= rules[i].getFitness(x, z, context);
        }

        return value;
    }

    private List<Viability> getRulesList() {
        return List.of(rules);
    }
}
