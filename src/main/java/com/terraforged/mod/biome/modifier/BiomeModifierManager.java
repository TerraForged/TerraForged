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
import com.terraforged.api.biome.modifier.ModifierManager;
import com.terraforged.core.cell.Cell;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.provider.DesertBiomes;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.heightmap.Levels;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BiomeModifierManager implements BiomeModifier, ModifierManager {

    private final DesertBiomes desertBiomes;
    private final List<BiomeModifier> biomeModifiers;

    public BiomeModifierManager(TerraContext context, BiomeMap biomes) {
        desertBiomes = new DesertBiomes(context.materials, biomes.getAllBiomes(BiomeType.DESERT), context.gameContext);
        List<BiomeModifier> modifiers = new ArrayList<>();
        modifiers.add(new CoastModifier(biomes, context));
        modifiers.add(new DesertColorModifier(desertBiomes));
        modifiers.add(new BeachModifier(biomes, context));
        modifiers.add(new DesertWetlandModifier(biomes));
        modifiers.add(new MountainModifier(context, biomes));
        modifiers.add(new VolcanoModifier(context, biomes));
        Collections.sort(modifiers);
        this.biomeModifiers = modifiers;
    }

    public boolean hasModifiers(Cell cell, Levels levels) {
        return cell.terrain.isOverground() || (cell.terrain.isSubmerged() && cell.value > levels.water);
    }

    public DesertBiomes getDesertBiomes() {
        return desertBiomes;
    }

    @Override
    public void register(BiomeModifier modifier) {
        biomeModifiers.add(modifier);
        Collections.sort(biomeModifiers);
    }

    @Override
    public int priority() {
        return -1;
    }

    @Override
    public boolean test(Biome biome, Cell cell) {
        return true;
    }

    @Override
    public Biome modify(Biome biome, Cell cell, int x, int z) {
        Biome result;
        for (BiomeModifier modifier : biomeModifiers) {
            if (modifier.test(biome, cell)) {
                result = modifier.modify(biome, cell, x, z);
                if (result != null) {
                    biome = result;
                }
                if (modifier.exitEarly()) {
                    return biome;
                }
            }
        }
        return biome;
    }
}
