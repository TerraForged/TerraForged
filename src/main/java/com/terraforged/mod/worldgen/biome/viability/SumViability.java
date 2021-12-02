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

import com.terraforged.cereal.Cereal;
import com.terraforged.cereal.spec.Context;
import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.value.DataValue;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.noise.util.NoiseUtil;
import it.unimi.dsi.fastutil.floats.FloatArrayList;

import java.util.List;

public record SumViability(Viability[] rules, float[] weights) implements Viability {
    public static final DataSpec<SumViability> SPEC = DataSpec.builder(
            "Sum",
            SumViability.class,
            (data, spec, context) -> new SumViability(
                    spec.get("rules", data, v -> getRules(v, context)),
                    spec.get("weights", data, v -> getWeights(v, context))))
            .addList("rules", SumViability::getRulesList)
            .addList("weights", SumViability::getWeightList)
            .build();

    @Override
    public float getFitness(int x, int z, TerrainData data, Generator generator) {
        float sumValue = 0F;
        float sumWeight = 0F;
        for (int i = 0; i < rules.length; i++) {
            float value = rules[i].getFitness(x, z, data, generator);
            float weight = weights[i];
            sumValue += value * weight;
            sumWeight += weight;
        }
        return NoiseUtil.clamp(sumValue / sumWeight, 0, 1);
    }

    private List<Viability> getRulesList() {
        return List.of(rules);
    }

    private List<Float> getWeightList() {
        return new FloatArrayList(weights);
    }

    public static Viability[] getRules(DataValue value, Context context) {
        return Cereal.deserialize(value.asList(), Viability.class, context).toArray(Viability[]::new);
    }

    public static float[] getWeights(DataValue value, Context context) {
        var list = value.asList();
        var weights = new float[list.size()];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = list.get(i).asFloat();
        }
        return weights;
    }
}
