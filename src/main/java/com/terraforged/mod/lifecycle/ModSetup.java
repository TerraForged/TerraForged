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

package com.terraforged.mod.lifecycle;

import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.worldgen.asset.*;
import net.minecraft.world.level.biome.Biome;

public class ModSetup extends Stage {
    public static final ModSetup STAGE = new ModSetup();

    @Override
    protected void doInit() {
        TerraForged.LOG.info("Setting up registries");
        var registryManager = CommonAPI.get().getRegistryManager();
        registryManager.create(TerraForged.CAVES, NoiseCave.CODEC);
        registryManager.create(TerraForged.CLIMATES, ClimateType.CODEC);
        registryManager.create(TerraForged.TERRAINS, TerrainNoise.CODEC);
        registryManager.create(TerraForged.TERRAIN_TYPES, TerrainType.DIRECT);
        registryManager.create(TerraForged.VEGETATIONS, VegetationConfig.CODEC);

        registryManager.create(TerraForged.BIOMES, Biome.DIRECT_CODEC, false);
    }
}
