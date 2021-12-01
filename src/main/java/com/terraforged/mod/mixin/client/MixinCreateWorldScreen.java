package com.terraforged.mod.mixin.client;

import com.mojang.datafixers.util.Pair;
import com.terraforged.mod.worldgen.datapack.DataPackExporter;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.DataPackConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Path;

@Mixin(CreateWorldScreen.class)
public abstract class MixinCreateWorldScreen {
    @Shadow
    private DataPackConfig dataPacks;

    @Shadow
    protected abstract Path getTempDataPackDir();

    @Shadow
    protected abstract Pair<File, PackRepository> getDataPackSelectionSettings();

    @Shadow
    protected abstract void tryApplyNewDataPacks(PackRepository packRepository);

    @Inject(method = "onCreate()V", at = @At("HEAD"))
    private void onCreate(CallbackInfo ci) {
        dataPacks = DataPackExporter.setup(getTempDataPackDir(), dataPacks);
        tryApplyNewDataPacks(getDataPackSelectionSettings().getSecond());
    }
}
