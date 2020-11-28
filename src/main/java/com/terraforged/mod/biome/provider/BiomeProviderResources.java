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

package com.terraforged.mod.biome.provider;

import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.modifier.BiomeModifierManager;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.util.setup.SetupHooks;
import com.terraforged.world.heightmap.WorldLookup;

public class BiomeProviderResources {

    final BiomeMap biomeMap;
    final WorldLookup worldLookup;
    final BiomeModifierManager modifierManager;

    public BiomeProviderResources(TerraContext context) {
        this.biomeMap = BiomeHelper.createBiomeMap(context.gameContext);
        this.worldLookup = new WorldLookup(context);
        this.modifierManager = SetupHooks.setup(new BiomeModifierManager(context, biomeMap), context.copy());
    }
}
