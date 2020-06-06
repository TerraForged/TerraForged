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
import com.terraforged.api.biome.modifier.ModifierManager;
import com.terraforged.biome.map.BiomeMap;
import com.terraforged.biome.provider.DesertBiomes;
import com.terraforged.chunk.TerraContext;
import com.terraforged.core.cell.Cell;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.terrain.ITerrain;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BiomeModifierManager implements BiomeModifier, ModifierManager {

    private final DesertBiomes desertBiomes;
    private final List<BiomeModifier> biomeModifiers;

    public BiomeModifierManager(TerraContext context, BiomeMap biomes) {
        desertBiomes = new DesertBiomes(context.materials, biomes.getAllBiomes(BiomeType.DESERT));
        List<BiomeModifier> modifiers = new ArrayList<>();
        modifiers.add(new BeachModifier(biomes, context));
        modifiers.add(new DesertColorModifier(desertBiomes));
        modifiers.add(new SandBiomeModifier(context));
        modifiers.add(new MushroomModifier());
        Collections.sort(modifiers);
        this.biomeModifiers = modifiers;
    }

    public boolean hasModifiers(ITerrain type) {
        return type.isOverground();
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
    public boolean test(Biome biome) {
        return true;
    }

    @Override
    public Biome modify(Biome biome, Cell cell, int x, int z) {
        for (BiomeModifier modifier : biomeModifiers) {
            if (modifier.test(biome)) {
                biome = modifier.modify(biome, cell, x, z);
            }
        }
        return biome;
    }
}
