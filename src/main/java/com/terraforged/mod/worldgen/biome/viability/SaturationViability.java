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
import com.terraforged.cereal.value.DataValue;

public record SaturationViability(float min, float max) implements Viability {
    public static final DataSpec<SaturationViability> SPEC = DataSpec.builder(
                    "Saturation",
                    SaturationViability.class,
                    (data, spec, context) -> new SaturationViability(
                            spec.get("min", data, DataValue::asFloat),
                            spec.get("max", data, DataValue::asFloat))
            )
            .add("min", 0F, SaturationViability::min)
            .add("max", 1F, SaturationViability::max)
            .build();

    public SaturationViability(float max) {
        this(0, max);
    }

    @Override
    public float getFitness(int x, int z, Context context) {
        // Water mask represents distance from river/lake so invert to get saturation
        float saturation = context.getTerrain().getWater().get(x, z);

        if (saturation < min) return 0F;
        if (saturation > max) return 1F;

        return (saturation - min) / (max - min);
    }
}
