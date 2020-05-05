package com.terraforged.mod.biome.spawn;

import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.BiomeProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnHandler {

    @SubscribeEvent
    public static void createSpawn(WorldEvent.CreateSpawnPosition event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world =(ServerWorld) event.getWorld();
            if (world.getChunkProvider().getChunkGenerator().getBiomeProvider() instanceof BiomeProvider) {
                Log.info("Searching for world spawn position");
                BiomeProvider provider = (BiomeProvider) world.getChunkProvider().getChunkGenerator().getBiomeProvider();
                SpawnSearch search = new SpawnSearch(BlockPos.ZERO, provider);
                BlockPos spawn = search.get();

                Log.info("Setting world spawn: {}", spawn);
                event.setCanceled(true);
                event.getWorld().getWorldInfo().setSpawn(spawn);

                if (event.getSettings().isBonusChestEnabled()) {
                    Log.info("Generating bonus chest");
                    createBonusChest(world, spawn);
                }
            }
        }
    }

    private static void createBonusChest(ServerWorld world, BlockPos pos) {
        ConfiguredFeature<?, ?> chest = Feature.BONUS_CHEST.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
        chest.place(world, world.getChunkProvider().getChunkGenerator(), world.rand, pos);
    }
}
