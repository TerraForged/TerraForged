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

package com.terraforged.mod;

import com.terraforged.mod.registry.GenRegistry;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.util.Init;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.BiomeTag;
import com.terraforged.mod.worldgen.asset.TerrainConfig;
import com.terraforged.mod.worldgen.asset.ViabilityConfig;
import com.terraforged.mod.worldgen.biome.Source;
import net.minecraft.core.Registry;

public class Common extends Init {
    public static final Common INSTANCE = new Common();

    Common() {}

    @Override
    protected void doInit() {
        TerraForged.LOG.info("Registering world-gen core codecs");
        Registry.register(Registry.BIOME_SOURCE, TerraForged.location("climate"), Source.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, TerraForged.location("generator"), Generator.CODEC);

        TerraForged.LOG.info("Registering world-gen component codecs");
        GenRegistry.register(ModRegistry.BIOME_TAG, BiomeTag.DIRECT_CODEC);
        GenRegistry.register(ModRegistry.TERRAIN, TerrainConfig.CODEC);
        GenRegistry.register(ModRegistry.VIABILITY, ViabilityConfig.CODEC); // depends on BiomeTag

        TerraForged.LOG.info("Registering world-gen registry extensions");
        GenRegistry.commit();
    }
}
