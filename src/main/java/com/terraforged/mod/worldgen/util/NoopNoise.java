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

package com.terraforged.mod.worldgen.util;

import com.google.common.base.Suppliers;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class NoopNoise {
    public static final NoiseRouter ROUTER = new NoiseRouter(
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            new XoroshiroRandomSource.XoroshiroPositionalRandomFactory(0L, 0L),
            new XoroshiroRandomSource.XoroshiroPositionalRandomFactory(0L, 0L),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            DensityFunctions.zero(),
            List.of()
    );

    public static final Supplier<DensityFunctions.BeardifierOrMarker> BEARDIFIER = Suppliers.ofInstance(new DensityFunctions.BeardifierOrMarker() {
        @Override
        public double compute(DensityFunction.FunctionContext ctx) {
            return 0.0D;
        }

        @Override
        public void fillArray(double[] array, DensityFunction.ContextProvider ctx) {
            Arrays.fill(array, 0.0D);
        }

        @Override
        public double minValue() {
            return 0.0D;
        }

        @Override
        public double maxValue() {
            return 0.0D;
        }
    });
}
