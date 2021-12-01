package com.terraforged.mod.mixin.common;

import com.terraforged.mod.util.RegistryUtil;
import net.minecraft.core.MappedRegistry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldGenSettings.class)
public class MixinWorldGenSettings {

    @ModifyVariable(
            method = "<init>(JZZLnet/minecraft/core/MappedRegistry;Ljava/util/Optional;)V",
            at = @At("HEAD")
    )
    private static MappedRegistry<LevelStem> onInit(MappedRegistry<LevelStem> dimensions, long seed) {
        return RegistryUtil.reseed(seed, dimensions);
    }
}
