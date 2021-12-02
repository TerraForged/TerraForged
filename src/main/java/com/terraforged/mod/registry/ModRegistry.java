package com.terraforged.mod.registry;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.worldgen.asset.BiomeTag;
import com.terraforged.mod.worldgen.asset.TerrainConfig;
import com.terraforged.mod.worldgen.asset.ViabilityConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface ModRegistry {
    ResourceKey<Registry<BiomeTag>> BIOME_TAG = TerraForged.registry("worldgen/biome_tag");
    ResourceKey<Registry<TerrainConfig>> TERRAIN = TerraForged.registry("worldgen/terrain");
    ResourceKey<Registry<ViabilityConfig>> VIABILITY = TerraForged.registry("worldgen/viability");
}