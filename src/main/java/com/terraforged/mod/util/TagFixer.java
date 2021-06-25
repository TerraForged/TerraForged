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
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class TagFixer<T> implements ITag.INamedTag<T> {
    public static final String FIX_BLOCK_TAG_KEY = "fixBlockTags";
    public static final boolean FIX_BLOCK_TAG_DEFAULT = true;

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
        return wrap(tag, TagCollectionManager.getInstance().getBlocks());
    }

    public static <T> ITag<T> wrap(ITag<T> tag, ITagCollection<T> collection) {
        if (!ConfigManager.GENERAL.getBool(FIX_BLOCK_TAG_KEY, FIX_BLOCK_TAG_DEFAULT)) return tag;

        if (tag instanceof INamedTag) return tag;

        ResourceLocation name = collection.getId(tag);
        if (name != null) return new TagFixer<>(name, tag);

        Log.err("Failed to find name for tag: {}", tag);
        return tag;
    }
}
