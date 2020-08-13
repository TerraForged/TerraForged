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

package com.terraforged.api.material.state;

public class States {

    public static final StateSupplier BEDROCK = DefaultState.of("minecraft:bedrock").cache();
    public static final StateSupplier COARSE_DIRT = DefaultState.of("minecraft:coarse_dirt").cache();
    public static final StateSupplier DIRT = DefaultState.of("minecraft:dirt").cache();
    public static final StateSupplier GRASS_BLOCK = DefaultState.of("minecraft:grass_block").cache();
    public static final StateSupplier PODZOL = DefaultState.of("minecraft:podzol").cache();
    public static final StateSupplier CLAY = DefaultState.of("minecraft:clay").cache();
    public static final StateSupplier GRAVEL = DefaultState.of("minecraft:gravel").cache();
    public static final StateSupplier LAVA = DefaultState.of("minecraft:lava").cache();
    public static final StateSupplier PACKED_ICE = DefaultState.of("minecraft:packed_ice").cache();
    public static final StateSupplier SAND = DefaultState.of("minecraft:sand").cache();
    public static final StateSupplier SMOOTH_SANDSTONE = DefaultState.of("minecraft:smooth_sandstone").cache();
    public static final StateSupplier SMOOTH_RED_SANDSTONE = DefaultState.of("minecraft:smooth_red_sandstone").cache();
    public static final StateSupplier SNOW_BLOCK = DefaultState.of("minecraft:snow_block").cache();
    public static final StateSupplier STONE = DefaultState.of("minecraft:stone").cache();
    public static final StateSupplier WATER = DefaultState.of("minecraft:water").cache();

    public static void invalidate() {
        CachedState.clearAll();
    }
}
