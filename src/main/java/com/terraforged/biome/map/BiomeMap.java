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

package com.terraforged.biome.map;

import com.google.gson.JsonElement;
import com.terraforged.core.cell.Cell;
import com.terraforged.world.biome.BiomeType;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Set;

public interface BiomeMap {

    Biome getBeach(float temperature, float moisture, float shape);

    Biome getRiver(float temperature, float moisture, float shape);

    Biome getLake(float temperature, float moisture, float shape);

    Biome getWetland(float temperature, float moisture, float shape);

    Biome getOcean(float temperature, float moisture, float shape);

    Biome getDeepOcean(float temperature, float moisture, float shape);

    Biome getBiome(BiomeType type, float temperature, float moisture, float shape);

    default Biome getBiome(Cell cell) {
        return getBiome(cell.biomeType, cell.temperature, cell.moisture, cell.biome);
    }

    List<Biome> getAllBiomes(BiomeType type);

    Set<Biome> getBiomes(BiomeType type);

    Set<Biome> getRivers(Biome.TempCategory temp);

    Set<Biome> getOceanBiomes(Biome.TempCategory temp);

    Set<Biome> getDeepOceanBiomes(Biome.TempCategory temp);

    JsonElement toJson();

    static Biome getBiome(Biome biome) {
        return biome.delegate.get();
    }

    interface Builder {

        Builder addBeach(Biome biome, int count);

        Builder addRiver(Biome biome, int count);

        Builder addLake(Biome biome, int count);

        Builder addWetland(Biome biome, int count);

        Builder addOcean(Biome biome, int count);

        Builder addBiome(BiomeType type, Biome biome, int count);

        BiomeMap build();
    }
}
