/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.fm.data;

import com.terraforged.fm.FeatureManager;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.packs.ModFileResourcePack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModDataPackFinder implements IPackFinder {

    @Override
    public void func_230230_a_(Consumer<ResourcePackInfo> consumer, ResourcePackInfo.IFactory factory) {
        for (ModFileInfo info : ModList.get().getModFiles()) {
            ResourcePackInfo packInfo = ResourcePackInfo.createResourcePack(
                    info.getFile().getFileName(),
                    true,
                    new ModSupplier(info.getFile()),
                    factory,
                    ResourcePackInfo.Priority.TOP,
                    IPackNameDecorator.BUILTIN
            );

            if (packInfo != null) {
                FeatureManager.LOG.debug(" Adding Mod RP: {}", packInfo.getName());
                consumer.accept(packInfo);
            }
        }
    }

    private static class ModSupplier implements Supplier<IResourcePack> {

        private final ModFile modFile;

        private ModSupplier(ModFile modFile) {
            this.modFile = modFile;
        }

        @Override
        public IResourcePack get() {
            return new ModFileResourcePack(modFile);
        }
    }
}
