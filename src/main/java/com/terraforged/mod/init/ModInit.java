package com.terraforged.mod.init;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.GenRegistry;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.TerrainConfig;
import com.terraforged.mod.worldgen.asset.ViabilityConfig;
import com.terraforged.mod.worldgen.biome.Source;
import net.minecraft.core.Registry;

public class ModInit {
    public static void init() {
        GenRegistry.get().register(ModRegistry.TERRAIN, TerrainConfig.CODEC);
        GenRegistry.get().register(ModRegistry.VIABILITY, ViabilityConfig.CODEC);
        Registry.register(Registry.BIOME_SOURCE, TerraForged.location("climate"), Source.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, TerraForged.location("generator"), Generator.CODEC);
    }
}
