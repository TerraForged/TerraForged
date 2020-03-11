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

package com.terraforged.mod.biome;

import com.terraforged.api.biome.BiomeVariant;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;

public class ModBiomes {

    private static final ArrayList<BiomeVariant> biomes = new ArrayList<>();

    public static final Biome COLD_STEPPE = register("cold_steppe", new ColdSteppe());
    public static final Biome SAVANNA_SCRUB = register("savanna_scrub", new SavannaScrub());
    public static final Biome SHATTERED_SAVANNA_SCRUB = register("shattered_savanna_scrub", new ShatteredSavannaScrub());
    public static final Biome SNOWY_TAIGA_SCRUB = register("snowy_taiga_scrub", new SnowyTaigaScrub());
    public static final Biome STEPPE = register("steppe", new Steppe());
    public static final Biome TAIGA_SCRUB = register("taiga_scrub", new TaigaScrub());
    public static final Biome WARM_BEACH = register("warm_beach", new WarmBeach());
    public static final Biome MARSHLAND = register("marshland", new Marshland());

    private static Biome register(String name, BiomeVariant biome) {
        return Registry.register(Registry.BIOME, new Identifier("terraforged", name), biome);
    }
}
