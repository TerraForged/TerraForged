package com.terraforged.mod.server;

import net.minecraft.server.ServerPropertiesProvider;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.server.dedicated.ServerProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Properties;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerPropertiesFix {

    @SubscribeEvent
    public static void setup(FMLDedicatedServerSetupEvent event) {
        DedicatedServer server = event.getServerSupplier().get();
        get(server, DedicatedServer.class, ServerPropertiesProvider.class).ifPresent(provider -> provider.func_219033_a(props -> {
            return get(props, PropertyManager.class, Properties.class).flatMap(properties -> {
                String world = properties.getProperty("mod-level-type");
                if (world != null && !world.isEmpty()) {
                    properties.setProperty("level-type", world);
                    return Optional.of(new ServerProperties(properties));
                }
                return Optional.empty();
            }).orElse(props);
        }));
    }

    private static <T> Optional<T> get(Object owner, Class<?> target, Class<T> type) {
        for (Field field : target.getDeclaredFields()) {
            if (field.getType() == type) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(owner);
                    if (value != null) {
                        return Optional.of(type.cast(value));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        return Optional.empty();
    }
}
