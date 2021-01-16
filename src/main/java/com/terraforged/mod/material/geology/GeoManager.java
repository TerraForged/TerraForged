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

package com.terraforged.mod.material.geology;

import com.terraforged.engine.Seed;
import com.terraforged.engine.world.geology.Geology;
import com.terraforged.engine.world.geology.Strata;
import com.terraforged.mod.api.material.geology.GeologyManager;
import com.terraforged.mod.api.material.geology.StrataConfig;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.material.Materials;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.block.BlockState;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public class GeoManager implements GeologyManager {

    private static final int SEED_OFFSET = -34678;

    private final Module selector;
    private final Materials materials;
    private final TFBiomeContext context;
    private final GeoGenerator builder;
    private final Geology<BlockState> general;
    private final Map<Biome, Geology<BlockState>> specific = new HashMap<>();

    public GeoManager(TerraContext context) {
        Seed seed = context.seed.offset(SEED_OFFSET);
        int scale = context.terraSettings.miscellaneous.strataRegionSize;
        this.selector = Source.cell(seed.next(), scale)
                .warp(seed.next(), scale / 4, 2, scale / 2D)
                .warp(seed.next(), 15, 2, 30);
        this.materials = context.materials.get();
        this.general = new Geology<>(selector);
        this.builder = new GeoGenerator(materials);
        this.context = context.biomeContext;
        init(seed);
    }

    @Override
    public GeoGenerator getStrataGenerator() {
        return builder;
    }

    @Override
    public void register(Strata<BlockState> strata) {
        general.add(strata);
    }

    @Override
    public void register(RegistryKey<Biome> biome, Strata<BlockState> strata) {
        register(biome, strata, false);
    }

    /**
     * Register a biome specific strata group.
     * If a specific geology doesn't exist for a given biome, the global geology can optionally be added using
     * the 'inheritGlobal' flag.
     */
    @Override
    public void register(RegistryKey<Biome> biome, Strata<BlockState> strata, boolean inheritGlobal) {
        Geology<BlockState> geology = specific.get(biome);
        if (geology == null) {
            geology = new Geology<>(selector);
            specific.put(context.biomes.get(biome), geology);
            if (inheritGlobal) {
                geology.add(general);
            }
        }
        geology.add(strata);
    }

    /**
     * Register/replace a biome-specific geology group
     */
    @Override
    public void register(RegistryKey<Biome> biome, Geology<BlockState> geology) {
        specific.put(context.biomes.get(biome), geology);
    }

    public Geology<BlockState> getGeology(Biome biome) {
        return specific.getOrDefault(biome, general);
    }

    public Strata<BlockState> getStrata(Biome biome, float value) {
        return getGeology(biome).getStrata(value);
    }

    private void init(Seed seed) {
        StrataConfig config = new StrataConfig();
        GeoGenerator generator = new GeoGenerator(materials);
        for (int i = 0; i < 100; i++) {
            Strata<BlockState> strata = generator.generate(seed.next(), 128, config);
            general.add(strata);
        }
    }
}
