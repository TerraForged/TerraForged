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
import com.terraforged.mod.Environment;
import com.terraforged.mod.registry.ModRegistries;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.util.seed.RandSeed;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;
import net.minecraft.core.RegistryAccess;

import java.util.function.BiFunction;

public interface ModTerrain extends ModRegistry {
    static void register() {
        var seed = Factory.createSeed();
        ModRegistries.register(TERRAIN, "steppe", Factory.create(seed, TerrainType.FLATS, Weights.STEPPE, LandForms::steppe));
        ModRegistries.register(TERRAIN, "plains", Factory.create(seed, TerrainType.FLATS, Weights.PLAINS, LandForms::plains));
        ModRegistries.register(TERRAIN, "hills_1", Factory.create(seed, TerrainType.HILLS, Weights.HILLS, LandForms::hills1));
        ModRegistries.register(TERRAIN, "hills_2", Factory.create(seed, TerrainType.HILLS, Weights.HILLS, LandForms::hills2));
        ModRegistries.register(TERRAIN, "dales", Factory.create(seed, TerrainType.HILLS, Weights.DALES, LandForms::dales));
        ModRegistries.register(TERRAIN, "plateau", Factory.create(seed, TerrainType.PLATEAU, Weights.PLATEAU, LandForms::plateau));
        ModRegistries.register(TERRAIN, "badlands", Factory.create(seed, TerrainType.BADLANDS, Weights.BADLANDS, LandForms::badlands));
        ModRegistries.register(TERRAIN, "torridonian", Factory.create(seed, TerrainType.HILLS, Weights.TORRIDONIAN, LandForms::torridonian));
        ModRegistries.register(TERRAIN, "mountains_1", Factory.create(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains));
        ModRegistries.register(TERRAIN, "mountains_2", Factory.create(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains2));
        ModRegistries.register(TERRAIN, "mountains_3", Factory.create(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains3));
        ModRegistries.register(TERRAIN, "dolomites", Factory.createDolomite(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS));
        ModRegistries.register(TERRAIN, "mountains_ridge_1", Factory.createNF(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains2));
        ModRegistries.register(TERRAIN, "mountains_ridge_2", Factory.createNF(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains3));
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

    static TerrainNoise[] getTerrain(RegistryAccess access) {
        if (access == null || Environment.DEV_ENV) {
            return Factory.getDefault();
        }
        return access.ownedRegistryOrThrow(TERRAIN.get()).stream().toArray(TerrainNoise[]::new);
    }

    class Factory {
        static final LandForms LAND_FORMS = new LandForms(settings(), new Levels(63, 255), Source.ZERO);
        static final LandForms LAND_FORMS_NF = new LandForms(nonFancy(), new Levels(63, 255), Source.ZERO);

        static Seed createSeed() {
            return new RandSeed(9712416L, 500_000);
        }

        static TerrainNoise create(Seed seed, Terrain type, float weight, BiFunction<LandForms, Seed, Module> factory) {
            return new TerrainNoise(type, weight, factory.apply(LAND_FORMS, seed));
        }

        static TerrainNoise createNF(Seed seed, Terrain type, float weight, BiFunction<LandForms, Seed, Module> factory) {
            return new TerrainNoise(type, weight, factory.apply(LAND_FORMS_NF, seed));
        }

        static TerrainNoise createDolomite(Seed seed, Terrain terrain, float weight) {
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
                    .clamp(0, 0.7).map(0, 1)
                    .warp(Domain.warp(Source.SIMPLEX, seed.next(), 40, 5, 35))
                    .alpha(0.9);

            var noise = shape.mult(peaks).max(slopes)
                    .warp(seed.next(), 800, 3, 300)
                    .scale(0.75);

            return new TerrainNoise(terrain, weight, noise);
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

        static TerrainNoise[] getDefault() {
            var seed = Factory.createSeed();
            return new TerrainNoise[] {
                    Factory.create(seed, TerrainType.FLATS, Weights.STEPPE, LandForms::steppe),
                    Factory.create(seed, TerrainType.FLATS, Weights.PLAINS, LandForms::plains),
                    Factory.create(seed, TerrainType.HILLS, Weights.HILLS, LandForms::hills1),
                    Factory.create(seed, TerrainType.HILLS, Weights.HILLS, LandForms::hills2),
                    Factory.create(seed, TerrainType.HILLS, Weights.DALES, LandForms::dales),
                    Factory.create(seed, TerrainType.PLATEAU, Weights.PLATEAU, LandForms::plateau),
                    Factory.create(seed, TerrainType.BADLANDS, Weights.BADLANDS, LandForms::badlands),
                    Factory.create(seed, TerrainType.HILLS, Weights.TORRIDONIAN, LandForms::torridonian),
                    Factory.create(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains),
                    Factory.create(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains2),
                    Factory.create(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains3),
                    Factory.createDolomite(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS),
                    Factory.createNF(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains2),
                    Factory.createNF(seed, TerrainType.MOUNTAINS, Weights.MOUNTAINS, LandForms::mountains3),
            };
        }
    }
}
