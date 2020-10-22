package com.terraforged.mixin.common;

import com.terraforged.mod.util.ExecutorUtils;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ExecutorService;

@Mixin(Util.class)
public class MixinUtils {

    @Inject(method = "createNamedService", at = @At("HEAD"), cancellable = true)
    private static void onCreateExecutor(String serviceName, CallbackInfoReturnable<ExecutorService> ci) {
        ExecutorService service = ExecutorUtils.create(serviceName);
        if (service != null) {
            ci.setReturnValue(service);
        }
    }
}
