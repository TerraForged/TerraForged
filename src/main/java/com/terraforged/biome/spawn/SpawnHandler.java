package com.terraforged.biome.spawn;

import com.terraforged.Log;
import com.terraforged.biome.provider.TerraBiomeProvider;
import com.terraforged.world.continent.MutableVeci;
import com.terraforged.world.continent.SpawnType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
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
            ChunkGenerator<?> generator = world.getChunkProvider().getChunkGenerator();
            if (generator.getBiomeProvider() instanceof TerraBiomeProvider) {
                Log.info("Searching for world spawn position");
                TerraBiomeProvider provider = (TerraBiomeProvider) generator.getBiomeProvider();
                BlockPos center = getSearchCenter(provider);
                SpawnSearch search = new SpawnSearch(center, provider);

                // SpawnSearch uses a rough look-up that does not account for the effects of erosion which
                // can add or remove from the heightmap depending on location. Use the chunk generator's
                // surface lookup function to obtain the actual height
                BlockPos spawn = getSurface(generator, search.get());

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

    private static BlockPos getSearchCenter(TerraBiomeProvider provider) {
        SpawnType spawnType = provider.getContext().terraSettings.world.properties.spawnType;
        if (spawnType == SpawnType.WORLD_ORIGIN) {
            return BlockPos.ZERO;
        } else {
            MutableVeci pos = new MutableVeci();
            provider.getContext().heightmap.getContinent().getNearestCenter(0, 0, pos);
            return new BlockPos(pos.x, 0, pos.z);
        }
    }

    private static BlockPos getSurface(ChunkGenerator<?> generator, BlockPos pos) {
        int surface = generator.func_222529_a(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
        return new BlockPos(pos.getX(), surface, pos.getZ());
    }

    private static void createBonusChest(ServerWorld world, BlockPos pos) {
        ConfiguredFeature<?, ?> chest = Feature.BONUS_CHEST.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
        chest.place(world, world.getChunkProvider().getChunkGenerator(), world.rand, pos);
    }
}
