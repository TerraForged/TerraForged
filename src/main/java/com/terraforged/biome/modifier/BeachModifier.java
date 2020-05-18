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

import com.terraforged.api.biome.modifier.BiomeModifier;
import com.terraforged.biome.map.BiomeMap;
import com.terraforged.chunk.TerraContext;
import com.terraforged.core.cell.Cell;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.TerrainTypes;
import net.minecraft.world.biome.Biome;

public class BeachModifier implements BiomeModifier {

    private final Levels levels;
    private final TerrainTypes terrain;
    private final BiomeMap biomeMap;

    public BeachModifier(BiomeMap biomeMap, TerraContext context) {
        this.levels = context.levels;
        this.terrain = context.terrain;
        this.biomeMap = biomeMap;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean test(Biome biome) {
        return true;
    }

    @Override
    public Biome modify(Biome in, Cell cell, int x, int z) {
        if (cell.terrainType == terrain.beach) {
            return biomeMap.getBeach(cell.temperature, cell.moisture, cell.biome);
        }
        if (cell.terrainType == terrain.coast && cell.value <= levels.water) {
            return biomeMap.getOcean(cell.temperature, cell.moisture, cell.biome);
        }
        return in;
    }
}
