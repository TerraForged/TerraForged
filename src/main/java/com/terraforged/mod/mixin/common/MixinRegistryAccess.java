package com.terraforged.mod.mixin.common;

import com.terraforged.mod.registry.GenRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryReadOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegistryAccess.class)
public class MixinRegistryAccess {
    @Inject(method = "load", at = @At("RETURN"))
    private static void onLoad(RegistryAccess access, RegistryReadOps<?> ops, CallbackInfo ci) {
        GenRegistry.INSTANCE.load(access, ops);
    }
}
