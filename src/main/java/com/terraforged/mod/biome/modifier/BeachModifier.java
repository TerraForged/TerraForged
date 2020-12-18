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

import com.terraforged.mod.api.biome.modifier.BiomeModifier;
import com.terraforged.engine.cell.Cell;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.engine.world.climate.biome.BiomeType;
import com.terraforged.engine.world.terrain.TerrainCategory;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class BeachModifier implements BiomeModifier {

    private final float height;
    private final Module noise;
    private final BiomeMap biomes;
    private final Biome mushroomFields;
    private final Biome mushroomFieldShore;

    public BeachModifier(BiomeMap biomeMap, TerraContext context) {
        this.biomes = biomeMap;
        this.height = context.levels.water(6);
        this.noise = Source.perlin(context.seed.next(), 15, 1).scale(context.levels.scale(5));
        this.mushroomFields = context.gameContext.biomes.get(Biomes.MUSHROOM_FIELDS);
        this.mushroomFieldShore = context.gameContext.biomes.get(Biomes.MUSHROOM_FIELD_SHORE);
    }

    @Override
    public int priority() {
        return 9;
    }

    @Override
    public boolean test(Biome biome, Cell cell) {
        return cell.terrain.getDelegate() == TerrainCategory.BEACH && cell.biome != BiomeType.DESERT;
    }

    @Override
    public Biome modify(Biome in, Cell cell, int x, int z) {
        if (cell.value + noise.getValue(x, z) < height) {
            if (in == mushroomFields) {
                return mushroomFieldShore;
            }
            return biomes.getBeach(cell);
        }
        return in;
    }
}
