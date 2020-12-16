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

package com.terraforged.mod.biome.modifier;

import com.terraforged.api.biome.modifier.BiomeModifier;
import com.terraforged.engine.cell.Cell;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.engine.world.climate.biome.BiomeType;
import com.terraforged.engine.world.terrain.TerrainCategory;
import net.minecraft.world.biome.Biome;

public class DesertWetlandModifier implements BiomeModifier {

    private final BiomeMap biomes;

    public DesertWetlandModifier(BiomeMap biomes) {
        this.biomes = biomes;
    }

    @Override
    public int priority() {
        // higher than desert colour mod
        return 6;
    }

    @Override
    public boolean exitEarly() {
        return true;
    }

    @Override
    public boolean test(Biome biome, Cell cell) {
        return cell.terrain.getDelegate() == TerrainCategory.WETLAND && cell.biome == BiomeType.DESERT;
    }

    @Override
    public Biome modify(Biome in, Cell cell, int x, int z) {
        return biomes.getLandSet().getBiome(getBiomeType(cell), cell.temperature, cell.biomeRegionId);
    }

    private static BiomeType getBiomeType(Cell cell) {
        return cell.biomeRegionId < 0.5F ? BiomeType.SAVANNA : BiomeType.STEPPE;
    }
}
