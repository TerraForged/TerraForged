package com.terraforged.api.client;

import com.terraforged.mod.util.reflect.Accessor;
import com.terraforged.mod.util.reflect.MethodAccessor;
import net.minecraft.client.gui.screen.WorldOptionsScreen;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class WorldOptionsFallback {

    private static final MethodAccessor<WorldOptionsScreen> ACCESSOR = Accessor.method(
            WorldOptionsScreen.class,
            Accessor.methodModifiers(Modifier::isProtected).and(Accessor.paramTypes(DimensionGeneratorSettings.class))
    );

    static void update(WorldOptionsScreen screen, DimensionGeneratorSettings settings) {
        try {
            ACCESSOR.invokeVoid(screen, settings);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
