/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.mixin.client;

import com.mojang.datafixers.util.Pair;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.client.screen.ScreenUtil;
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
    protected DataPackConfig dataPacks;

    @Shadow
    protected abstract Path getTempDataPackDir();

    @Shadow
    protected abstract Pair<File, PackRepository> getDataPackSelectionSettings();

    @Shadow
    protected abstract void tryApplyNewDataPacks(PackRepository packRepository);

    @Inject(method = "onCreate()V", at = @At("HEAD"))
    private void onCreate(CallbackInfo ci) {
        if (ScreenUtil.isPresetEnabled(((CreateWorldScreen) (Object) this))) {
            dataPacks = DataPackExporter.setup(getTempDataPackDir(), dataPacks);

            var selection = getDataPackSelectionSettings().getSecond();
            selection.setSelected(dataPacks.getEnabled());
            tryApplyNewDataPacks(selection);

            TerraForged.LOG.info("Applied datapacks: {}", selection.getSelectedIds());
        }
    }
}
