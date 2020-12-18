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

package com.terraforged.mod.biome.map;

import com.google.gson.JsonElement;
import com.terraforged.engine.cell.Cell;
import com.terraforged.mod.featuremanager.GameContext;
import com.terraforged.mod.biome.map.set.BiomeTypeSet;
import com.terraforged.engine.world.climate.biome.BiomeType;
import com.terraforged.engine.world.heightmap.Levels;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.List;

public interface BiomeMap {

    Biome getBeach(Cell cell);

    Biome getCoast(Cell cell);

    Biome getRiver(Cell cell);

    Biome getLake(Cell cell);

    Biome getWetland(Cell cell);

    Biome getShallowOcean(Cell cell);

    Biome getDeepOcean(Cell cell);

    Biome getLand(Cell cell);

    @Nullable
    Biome getMountain(Cell cell);

    @Nullable
    Biome getVolcano(Cell cell);

    Biome provideBiome(Cell cell, Levels levels);

    BiomeTypeSet getLandSet();

    List<Biome> getAllBiomes(BiomeType type);

    JsonElement toJson(GameContext context);

    interface Builder {

        Builder addOcean(Biome biome, int count);

        Builder addBeach(Biome biome, int count);

        Builder addCoast(Biome biome, int count);

        Builder addRiver(Biome biome, int count);

        Builder addWetland(Biome biome, int count);

        Builder addLake(Biome biome, int count);

        Builder addMountain(Biome biome, int count);

        Builder addVolcano(Biome biome, int count);

        Builder addLand(BiomeType type, Biome biome, int count);

        BiomeMap build();
    }
}
