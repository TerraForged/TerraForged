package com.terraforged.api.feature.decorator;

import com.terraforged.mod.util.reflect.Accessor;
import com.terraforged.mod.util.reflect.FieldAccessor;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class DecorationHelperFallback {

    private static final FieldAccessor<WorldDecoratingHelper, ISeedReader> REGION = Accessor.field(
            WorldDecoratingHelper.class,
            ISeedReader.class,
            Accessor.fieldModifiers(Modifier::isPrivate, Modifier::isFinal)
    );

    private static final FieldAccessor<WorldDecoratingHelper, ChunkGenerator> GENERATOR = Accessor.field(
            WorldDecoratingHelper.class,
            ChunkGenerator.class,
            Accessor.fieldModifiers(Modifier::isPrivate, Modifier::isFinal)
    );

    static DecorationContext getContext(WorldDecoratingHelper helper) {
        try {
            ISeedReader region = REGION.get(helper);
            ChunkGenerator generator = GENERATOR.get(helper);
            return DecorationContext.of(region, generator);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access context info for " + helper);
        }
    }
}
