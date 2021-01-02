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

package com.terraforged.mod.biome;

import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.biome.utils.BiomeBuilder;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBiomes {

    private static final Map<RegistryKey<Biome>, RegistryKey<Biome>> remaps = new HashMap<>();

    public static final RegistryKey<Biome> BRYCE = createKey("bryce");
    public static final RegistryKey<Biome> COLD_STEPPE = createKey("cold_steppe");
    public static final RegistryKey<Biome> COLD_MARSHLAND = createKey("cold_marshland");
    public static final RegistryKey<Biome> FIR_FOREST = createKey("fir_forest");
    public static final RegistryKey<Biome> FLOWER_PLAINS = createKey("flower_plains");
    public static final RegistryKey<Biome> FROZEN_LAKE = createKey("frozen_lake");
    public static final RegistryKey<Biome> FROZEN_MARSH = createKey("frozen_marsh");
    public static final RegistryKey<Biome> LAKE = createKey("lake");
    public static final RegistryKey<Biome> MARSHLAND = createKey("marshland");
    public static final RegistryKey<Biome> SAVANNA_SCRUB = createKey("savanna_scrub");
    public static final RegistryKey<Biome> SHATTERED_SAVANNA_SCRUB = createKey("shattered_savanna_scrub");
    public static final RegistryKey<Biome> SNOWY_FIR_FOREST = createKey("snowy_fir_forest");
    public static final RegistryKey<Biome> SNOWY_TAIGA_SCRUB = createKey("snowy_taiga_scrub");
    public static final RegistryKey<Biome> STEPPE = createKey("steppe");
    public static final RegistryKey<Biome> STONE_FOREST = createKey("stone_forest");
    public static final RegistryKey<Biome> TAIGA_SCRUB = createKey("taiga_scrub");
    public static final RegistryKey<Biome> WARM_BEACH = createKey("warm_beach");

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Biome> event) {
        register(event, BRYCE, BiomeBuilders.bryce());
        register(event, COLD_STEPPE, BiomeBuilders.coldSteppe());
        register(event, COLD_MARSHLAND, BiomeBuilders.coldMarsh());
        register(event, FIR_FOREST, BiomeBuilders.firForest());
        register(event, FLOWER_PLAINS, BiomeBuilders.flowerPlains());
        register(event, FROZEN_LAKE, BiomeBuilders.frozenLake());
        register(event, FROZEN_MARSH, BiomeBuilders.frozenMarsh());
        register(event, LAKE, BiomeBuilders.lake());
        register(event, MARSHLAND, BiomeBuilders.marshland());
        register(event, SAVANNA_SCRUB, BiomeBuilders.savannaScrub());
        register(event, SHATTERED_SAVANNA_SCRUB, BiomeBuilders.shatteredSavannaScrub());
        register(event, SNOWY_FIR_FOREST, BiomeBuilders.snowyFirForest());
        register(event, SNOWY_TAIGA_SCRUB, BiomeBuilders.snowyTaigaScrub());
        register(event, STEPPE, BiomeBuilders.steppe());
        register(event, STONE_FOREST, BiomeBuilders.stoneForest());
        register(event, TAIGA_SCRUB, BiomeBuilders.taigaScrub());
        register(event, WARM_BEACH, BiomeBuilders.warmBeach());
    }

    public static Biome remap(Biome biome, TFBiomeContext context) {
        RegistryKey<Biome> keyIn = context.biomes.getKey(biome);
        if (keyIn != null) {
            RegistryKey<Biome> keyOut = remaps.get(keyIn);
            if (keyOut != null) {
                Biome biomeOut = context.biomes.get(keyOut);
                if (biomeOut != null) {
                    return biomeOut;
                }
            }
        }
        return biome;
    }

    private static RegistryKey<Biome> createKey(String name) {
        return RegistryKey.getOrCreateKey(Registry.BIOME_KEY, new ResourceLocation(TerraForgedMod.MODID, name));
    }

    private static void register(RegistryEvent.Register<Biome> event, RegistryKey<Biome> key, BiomeBuilder builder) {
        event.getRegistry().register(builder.build(key));
        builder.registerTypes(key);
        builder.registerWeight(key);
        remaps.put(key, builder.getParentKey());
    }
}
