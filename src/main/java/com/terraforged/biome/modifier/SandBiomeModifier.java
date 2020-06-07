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

package com.terraforged.biome.modifier;

import com.terraforged.chunk.TerraContext;
import com.terraforged.core.cell.Cell;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.stream.Collectors;

// prevents deserts forming at high levels
public class SandBiomeModifier extends AbstractMaxHeightModifier {

    private final Set<Biome> biomes;

    public SandBiomeModifier(TerraContext context) {
        super(context.seed, context.factory.getClimate(), 10, 1, context.levels.scale(8), context.levels.ground(10), context.levels.ground(25));
        this.biomes = ForgeRegistries.BIOMES.getValues().stream()
                .filter(biome -> context.materials.isSand(biome.getSurfaceBuilderConfig().getTop().getBlock()))
                .collect(Collectors.toSet());
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public boolean test(Biome biome) {
        return biome.getCategory() == Biome.Category.DESERT && biomes.contains(biome);
    }

    @Override
    protected Biome getModifiedBiome(Biome in, Cell cell, int x, int z, float ox, float oz) {
        return Biomes.BADLANDS;
    }
}
