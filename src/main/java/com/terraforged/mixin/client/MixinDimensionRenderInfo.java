package com.terraforged.mixin.client;

import com.terraforged.mod.config.ConfigManager;
import net.minecraft.client.world.DimensionRenderInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionRenderInfo.class)
public class MixinDimensionRenderInfo {

    @Inject(method = "func_239213_a_", at = @At("HEAD"), cancellable = true)
    private void getCloudHeight(CallbackInfoReturnable<Float> ci) {
        ConfigManager.GENERAL.getValue("cloud_height", 180F, ci::setReturnValue);
    }
}
