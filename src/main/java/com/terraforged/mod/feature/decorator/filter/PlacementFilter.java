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

package com.terraforged.mod.feature.decorator.filter;

import com.terraforged.mod.api.feature.decorator.DecorationContext;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

public class PlacementFilter {

    private static final Map<String, PlacementFilter> REGISTRY = new ConcurrentHashMap<>();

    static {
        register("biome", (ctx, pos) -> ctx.getBiome(pos) == ctx.getBiomes().getFeatureBiome());
    }

    private final String name;
    private final BiPredicate<DecorationContext, BlockPos> filter;

    public PlacementFilter(String name, BiPredicate<DecorationContext, BlockPos> filter) {
        this.name = name;
        this.filter = filter;
    }

    public String getName() {
        return name;
    }

    public boolean test(DecorationContext context, BlockPos pos) {
        return true; // filter.test(context, pos);
    }

    private static void register(String name, BiPredicate<DecorationContext, BlockPos> filter) {
        REGISTRY.put(name, new PlacementFilter(name, filter));
    }

    public static Optional<PlacementFilter> decode(String name) {
        return Optional.ofNullable(REGISTRY.get(name));
    }
}
