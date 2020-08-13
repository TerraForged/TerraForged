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

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBiomes {

    public static final RegistryKey<Biome> BRYCE = create("bryce");
    public static final RegistryKey<Biome> COLD_STEPPE = create("cold_steppe");
    public static final RegistryKey<Biome> COLD_MARSHLAND = create("cold_marshland");
    public static final RegistryKey<Biome> ERODED_PINNACLE = create("stone_forest");
    public static final RegistryKey<Biome> FIR_FOREST = create("fir_forest");
    public static final RegistryKey<Biome> FLOWER_PLAINS = create("flower_plains");
    public static final RegistryKey<Biome> FROZEN_LAKE = create("frozen_lake");
    public static final RegistryKey<Biome> LAKE = create("lake");
    public static final RegistryKey<Biome> MARSHLAND = create("marshland");
    public static final RegistryKey<Biome> SAVANNA_SCRUB = create("savanna_scrub");
    public static final RegistryKey<Biome> SHATTERED_SAVANNA_SCRUB = create("shattered_savanna_scrub");
    public static final RegistryKey<Biome> SNOWY_FIR_FOREST = create("snowy_fir_forest");
    public static final RegistryKey<Biome> SNOWY_TAIGA_SCRUB = create("snowy_taiga_scrub");
    public static final RegistryKey<Biome> STEPPE = create("steppe");
    public static final RegistryKey<Biome> TAIGA_SCRUB = create("taiga_scrub");
    public static final RegistryKey<Biome> WARM_BEACH = create("warm_beach");

    private static RegistryKey<Biome> create(String name) {
        return RegistryKey.func_240903_a_(Registry.BIOME_KEY, new ResourceLocation("terraforged", name));
    }

    private static RegistryKey<Biome> register(Biome biome) {
        return RegistryKey.func_240903_a_(Registry.BIOME_KEY, new ResourceLocation("todo"));
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register event) {
//        biomes.forEach(biome -> {
//            event.getRegistry().register(biome.get());
//
//            // TF biomes can specify a custom biome weight
//            biome.registerWeights();
//
//            // let forge generate the BiomeDictionary.Type's since TF biomes are just variants of vanilla ones
//            BiomeDictionary.makeBestGuess(biome.get());
//
//            // overworld is required for TerraForged
//            BiomeDictionary.addTypes(biome.get(), BiomeDictionary.Type.OVERWORLD);
//
//            // if a variant of a rare biome, register as such
//            if (BiomeDictionary.getTypes(biome.getBase()).contains(BiomeDictionary.Type.RARE)) {
//                BiomeDictionary.addTypes(biome.get(), BiomeDictionary.Type.RARE);
//            }
//        });
    }
}
