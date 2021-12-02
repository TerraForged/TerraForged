package com.terraforged.mod.mixin.common;

import com.terraforged.mod.registry.GenRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(RegistryAccess.RegistryHolder.class)
public class MixinRegistryHolder {

    @ModifyVariable(
            method = "<init>(Ljava/util/Map;)V",
            at = @At("HEAD"),
            ordinal = 0
    )
    private static Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> onInit(
            Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> registries) {

        return GenRegistry.INSTANCE.extend(registries);
    }
}
