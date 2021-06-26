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

package com.terraforged.mod.util;

import com.terraforged.mod.Log;
import com.terraforged.mod.config.ConfigManager;
import net.minecraft.block.Block;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class TagFixer<T> implements ITag.INamedTag<T> {
    public static final String FIX_BLOCK_TAG_KEY = "fixBlockTags";
    public static final boolean FIX_BLOCK_TAG_DEFAULT = true;
    private static volatile boolean postInit = false;

    private final ResourceLocation name;
    private final ITag<T> delegate;

    public TagFixer(ResourceLocation name, ITag<T> delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }

    @Override
    public boolean contains(T t) {
        return delegate.contains(t);
    }

    @Override
    public List<T> getValues() {
        return delegate.getValues();
    }

    @Override
    public String toString() {
        return "TagFixer{" +
                "name=" + name +
                ", delegate=" + delegate +
                '}';
    }

    public static ITag<Block> wrap(ITag<Block> tag) {
        // Avoid possible early access to TagCollectionManager (if someone is using plain a Tag for some reason).
        if (!postInit) return tag;

        // Don't need to wrap if it's already a named tag.
        // All early lifecyle tags are (should be) named tags so this acts as a second lock against wrapping too early.
        if (tag instanceof INamedTag) return tag;

        // If the fix is disabled do nothing.
        if (!ConfigManager.GENERAL.getBool(FIX_BLOCK_TAG_KEY, FIX_BLOCK_TAG_DEFAULT)) return tag;

        // If we've made it this far it should be fine to retrieve the id.
        ResourceLocation name = TagCollectionManager.getInstance().getBlocks().getId(tag);
        if (name != null) return new TagFixer<>(name, tag);

        Log.warn("Failed to find name for tag: {}", tag.getValues().stream().map(Block::getRegistryName).collect(Collectors.toList()));

        return tag;
    }

    public static void markPostInit() {
        postInit = true;
    }
}
