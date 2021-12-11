/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.data;

import com.terraforged.engine.Seed;
import com.terraforged.engine.settings.TerrainSettings;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.terrain.LandForms;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.registry.ModRegistries;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.util.seed.RandSeed;
import com.terraforged.mod.worldgen.asset.TerrainConfig;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

import java.util.function.BiFunction;

public interface ModTerrain extends ModRegistry {
    static void register() {
        ModRegistries.register(TERRAIN, "steppe", Factory.create(TerrainType.FLATS, Weights.STEPPE, LandForms::steppe));
        ModRegistries.register(TERRAIN, "plains", Factory.create(TerrainType.FLATS, Weights.PLAINS, LandForms::plains));
        ModRegistries.register(TERRAIN, "hills_1", Factory.create(TerrainType.HILLS, Weights.HILLS, LandForms::hills1));
        ModRegistries.register(TERRAIN, "hills_2", Factory.create(TerrainType.HILLS, Weights.HILLS, LandForms::hills2));
        ModRegistries.register(TERRAIN, "dales", Factory.create(TerrainType.HILLS, Weights.DALES, LandForms::dales));
        ModRegistries.register(TERRAIN, "plateau", Factory.create(TerrainType.PLATEAU, Weights.PLATEAU, LandForms::plateau));
        ModRegistries.register(TERRAIN, "badlands", Factory.create(TerrainType.BADLANDS, Weights.BADLANDS, LandForms::badlands));
        ModRegistries.register(TERRAIN, "torridonian", Factory.create(TerrainType.HILLS, Weights.TORRIDONIAN, LandForms::torridonian));
        ModRegistries.register(TERRAIN, "mountains_1", Factory.create(TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains));
        ModRegistries.register(TERRAIN, "mountains_2", Factory.create(TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains2));
        ModRegistries.register(TERRAIN, "mountains_3", Factory.create(TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains3));
    }

    interface Weights {
        float STEPPE = 1F;
        float PLAINS = 2F;
        float HILLS = 2F;
        float DALES = 1.5F;
        float PLATEAU = 1.5F;
        float BADLANDS = 1F;
        float TORRIDONIAN = 2F;
        float MOUNTAINS = 2.5F;
    }

    class Factory {
        static final Seed SEED = new RandSeed(9712416L, 500_000);
        static final LandForms LAND_FORMS = new LandForms(new TerrainSettings(), new Levels(63, 255), Source.ZERO);

        static TerrainConfig create(Terrain type, float weight, BiFunction<LandForms, Seed, Module> factory) {

            return new TerrainConfig(type, weight, factory.apply(LAND_FORMS, SEED));
        }
    }
}
