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
