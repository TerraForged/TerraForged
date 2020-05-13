package com.terraforged.mod.chunk.fix;

import net.minecraft.world.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnFix {

    private static final AtomicBoolean MOB_SPAWNING = new AtomicBoolean();

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isServer()) {
            boolean mobSpawning = event.world.getGameRules().get(GameRules.DO_MOB_SPAWNING).get();
            MOB_SPAWNING.set(mobSpawning);
        }
    }

    public static boolean canSpawnMobs() {
        return MOB_SPAWNING.get();
    }
}
