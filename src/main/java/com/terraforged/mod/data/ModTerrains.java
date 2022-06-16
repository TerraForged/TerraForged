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
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.seed.RandSeed;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;

import java.util.function.BiFunction;

import static com.terraforged.mod.TerraForged.TERRAINS;

public interface ModTerrains {
    static void register() {
        var seed = Factory.createSeed();
        TerraForged.register(TERRAINS, "steppe", Factory.create(null, seed, TerrainType.FLATS, Weights.STEPPE, LandForms::steppe));
        TerraForged.register(TERRAINS, "plains", Factory.create(null, seed, TerrainType.FLATS, Weights.PLAINS, LandForms::plains));
        TerraForged.register(TERRAINS, "hills_1", Factory.create(null, seed, TerrainType.HILLS, Weights.HILLS, LandForms::hills1));
        TerraForged.register(TERRAINS, "hills_2", Factory.create(null, seed, TerrainType.HILLS, Weights.HILLS, LandForms::hills2));
        TerraForged.register(TERRAINS, "dales", Factory.create(null, seed, TerrainType.HILLS, Weights.DALES, LandForms::dales));
        TerraForged.register(TERRAINS, "plateau", Factory.create(null, seed, TerrainType.PLATEAU, Weights.PLATEAU, LandForms::plateau));
        TerraForged.register(TERRAINS, "badlands", Factory.create(null, seed, TerrainType.BADLANDS, Weights.BADLANDS, LandForms::badlands));
        TerraForged.register(TERRAINS, "torridonian", Factory.create(null, seed, ModTerrainTypes.TORRIDONIAN, Weights.TORRIDONIAN, LandForms::torridonian));
        TerraForged.register(TERRAINS, "mountains_1", Factory.create(null, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains));
        TerraForged.register(TERRAINS, "mountains_2", Factory.create(null, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains2));
        TerraForged.register(TERRAINS, "mountains_3", Factory.create(null, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains3));
        TerraForged.register(TERRAINS, "dolomites", Factory.createDolomite(null, seed, ModTerrainTypes.DOLOMITES, Weights.MOUNTAINS));
        TerraForged.register(TERRAINS, "mountains_ridge_1", Factory.createNF(null, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains2));
        TerraForged.register(TERRAINS, "mountains_ridge_2", Factory.createNF(null, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains3));
    }

    interface Weights {
        float STEPPE = 1.5F;
        float PLAINS = 2.5F;
        float HILLS = 2F;
        float DALES = 1.5F;
        float PLATEAU = 2F;
        float BADLANDS = 1.75F;
        float TORRIDONIAN = 2.5F;
        float MOUNTAINS = 1.25F;
    }

    class Factory {
        static final LandForms LAND_FORMS = new LandForms(settings(), new Levels(63, 255), Source.ZERO);
        static final LandForms LAND_FORMS_NF = new LandForms(nonFancy(), new Levels(63, 255), Source.ZERO);

        static Seed createSeed() {
            return new RandSeed(9712416L, 500_000);
        }

        static TerrainNoise create(RegistryAccess access, Seed seed, Terrain type, float weight, BiFunction<LandForms, Seed, Module> factory) {
            return new TerrainNoise(getType(access, type), weight, factory.apply(LAND_FORMS, seed));
        }

        static TerrainNoise createNF(RegistryAccess access, Seed seed, Terrain type, float weight, BiFunction<LandForms, Seed, Module> factory) {
            return new TerrainNoise(getType(access, type), weight, factory.apply(LAND_FORMS_NF, seed));
        }

        static TerrainNoise createDolomite(RegistryAccess access, Seed seed, Terrain type, float weight) {
            // Valley floor terrain
            var base = Source.simplex(seed.next(), 80, 4).scale(0.1);

            // Controls where the ridges show up
            var shape = Source.simplex(seed.next(), 475, 4)
                    .clamp(0.3, 1.0).map(0, 1)
                    .warp(seed.next(), 10, 2, 8);

            // More gradual slopes up to the ridges
            var slopes = shape.pow(2.2).scale(0.65).add(base);

            // Sharp ridges
            var peaks = Source.build(seed.next(), 400, 5).lacunarity(2.7).gain(0.6).simplexRidge()
                    .clamp(0, 0.675).map(0, 1)
                    .warp(Domain.warp(Source.SIMPLEX, seed.next(), 40, 5, 30))
                    .alpha(0.875);

            var noise = shape.mult(peaks).max(slopes)
                    .warp(seed.next(), 800, 3, 300)
                    .scale(0.75);

            return new TerrainNoise(getType(access, type), weight, noise);
        }

        static Holder<com.terraforged.mod.worldgen.asset.TerrainType> getType(RegistryAccess access, Terrain terrain) {
            return TerraForged.TERRAIN_TYPES.holder(terrain.getName(), access, () -> com.terraforged.mod.worldgen.asset.TerrainType.of(terrain));
        }

        static TerrainSettings settings() {
            var settings = new TerrainSettings();
            settings.general.globalVerticalScale = 1F;
            return settings;
        }

        static TerrainSettings nonFancy() {
            var settings = settings();
            settings.general.fancyMountains = false;
            return settings;
        }

        public static TerrainNoise[] getDefault(RegistryAccess access) {
            var seed = Factory.createSeed();
            return new TerrainNoise[] {
                    Factory.create(access, seed, TerrainType.FLATS, Weights.STEPPE, LandForms::steppe),
                    Factory.create(access, seed, TerrainType.FLATS, Weights.PLAINS, LandForms::plains),
                    Factory.create(access, seed, TerrainType.HILLS, Weights.HILLS, LandForms::hills1),
                    Factory.create(access, seed, TerrainType.HILLS, Weights.HILLS, LandForms::hills2),
                    Factory.create(access, seed, TerrainType.HILLS, Weights.DALES, LandForms::dales),
                    Factory.create(access, seed, TerrainType.PLATEAU, Weights.PLATEAU, LandForms::plateau),
                    Factory.create(access, seed, TerrainType.BADLANDS, Weights.BADLANDS, LandForms::badlands),
                    Factory.create(access, seed, ModTerrainTypes.TORRIDONIAN, Weights.TORRIDONIAN, LandForms::torridonian),
                    Factory.create(access, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains),
                    Factory.create(access, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains2),
                    Factory.create(access, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains3),
                    Factory.createDolomite(access, seed, ModTerrainTypes.DOLOMITES, Weights.MOUNTAINS),
                    Factory.createNF(access, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains2),
                    Factory.createNF(access, seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains3),
            };
        }
    }
}
