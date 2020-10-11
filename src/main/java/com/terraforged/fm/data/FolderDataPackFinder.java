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

import net.minecraft.resources.FolderPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.ResourcePackInfo;
import org.jline.utils.Log;

import java.io.File;
import java.util.function.Consumer;

public class FolderDataPackFinder extends FolderPackFinder {

    public static final IPackNameDecorator TF_FOLDER = IPackNameDecorator.create("pack.source.folder");

    public FolderDataPackFinder(File folderIn) {
        this(folderIn, TF_FOLDER);
    }

    public FolderDataPackFinder(File folderIn, IPackNameDecorator decorator) {
        super(folderIn, decorator);
    }

    @Override
    public void func_230230_a_(Consumer<ResourcePackInfo> consumer, ResourcePackInfo.IFactory factory) {
        Log.debug("Searching for DataPacks...");
        super.func_230230_a_(resourceTracker(consumer), factory);
    }

    private static Consumer<ResourcePackInfo> resourceTracker(Consumer<ResourcePackInfo> delegate) {
        return info -> {
            Log.debug("Adding datapack: {}", info.getName());
            delegate.accept(info);
        };
    }
}
