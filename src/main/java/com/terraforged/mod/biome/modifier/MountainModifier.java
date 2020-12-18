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

import com.terraforged.engine.cell.Cell;
import com.terraforged.mod.api.biome.modifier.BiomeModifier;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.world.biome.Biome;

public class MountainModifier implements BiomeModifier {

    // the probability that mountain terrain will get upgraded to a mountain biome
    public static final float MOUNTAIN_CHANCE = 0.4F;
    // height above ground level where mountain biomes can start to take over
    private static final int MOUNTAIN_START_HEIGHT = 48;

    // probability of mountain biome overriding others
    private final float chance;
    // float height where mountains can override
    private final float height;
    // amount of noise variance to be applied
    private final float range;
    // noise used to modulate the position's height
    private final Module noise;

    private final BiomeMap biomes;

    public MountainModifier(TerraContext context, BiomeMap biomes) {
        this.biomes = biomes;
        this.chance = context.terraSettings.miscellaneous.mountainBiomeUsage;
        this.range = context.levels.scale(10);
        this.height = context.levels.ground(MOUNTAIN_START_HEIGHT);
        this.noise = Source.perlin(context.seed.next(), 80, 2).scale(range);
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean exitEarly() {
        return true;
    }

    @Override
    public boolean test(Biome biome, Cell cell) {
        return cell.terrain.isMountain() && cell.macroBiomeId < chance;
    }

    @Override
    public Biome modify(Biome in, Cell cell, int x, int z) {
        if (canModify(cell, x, z)) {
            Biome mountain = biomes.getMountain(cell);
            if (TerraBiomeProvider.isValidBiome(mountain)) {
                return mountain;
            }
        }
        return in;
    }

    private boolean canModify(Cell cell, int x, int z) {
        if (cell.value > height) {
            return true;
        }
        if (cell.value + range < height) {
            return false;
        }
        return cell.value + noise.getValue(x, z) > height;
    }
}
