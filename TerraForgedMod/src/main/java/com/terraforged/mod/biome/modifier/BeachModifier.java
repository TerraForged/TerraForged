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

package com.terraforged.mod.biome.modifier;

import com.terraforged.api.biome.modifier.BiomeModifier;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.mod.biome.map.BiomeMap;
import net.minecraft.world.biome.Biome;

public class BeachModifier implements BiomeModifier {

    private final Terrains terrain;
    private final BiomeMap biomeMap;

    public BeachModifier(BiomeMap biomeMap, Terrains terrain) {
        this.terrain = terrain;
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
    public Biome modify(Biome in, Cell<Terrain> cell, int x, int z) {
        if (cell.tag == terrain.beach) {
            return biomeMap.getBeach(cell.temperature, cell.moisture, cell.biome);
        }
        return in;
    }
}
