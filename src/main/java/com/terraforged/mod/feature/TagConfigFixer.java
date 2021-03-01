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

package com.terraforged.mod.feature;

import com.google.common.collect.ImmutableList;
import com.terraforged.mod.Log;
import com.terraforged.mod.api.material.WGTags;
import com.terraforged.mod.config.ConfigManager;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TagConfigFixer {

    public static final String FIX_BLOCK_TAG_KEY = "fixBlockTags";
    public static final boolean FIX_BLOCK_TAG_DEFAULT = true;

    private static final Map<ITag<Block>, ITag<Block>> cache = new ConcurrentHashMap<>();
    private static final List<ITag.INamedTag<Block>> TARGET_TAGS = ImmutableList.of(BlockTags.BASE_STONE_OVERWORLD);

    public static void reset() {
        Log.debug("Clearing cached tag-references");
        cache.clear();
    }

    public static ITag<Block> getFixedBlockTag(ITag<Block> tag) {
        if (ConfigManager.GENERAL.getBool(FIX_BLOCK_TAG_KEY, FIX_BLOCK_TAG_DEFAULT)) {
            // Named tags serialize properly :)
            if (tag instanceof ITag.INamedTag) {
                return tag;
            }

            ITag<Block> replacement = cache.computeIfAbsent(tag, TagConfigFixer::computeTag);
            if (replacement != null) {
                return replacement;
            }
        }
        return tag;
    }

    private static ITag<Block> computeTag(ITag<Block> tag) {
        List<Block> source = tag.getAllElements();

        // Check for ambiguous tags
        List<ResourceLocation> matches = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ITag<Block>> entry : BlockTags.getCollection().getIDTagMap().entrySet()) {
            // Ignore our own tags since we know we don't add them to TagMatchRuleTest's
            if (WGTags.NAMED_WG_TAGS.contains(entry.getKey())) {
                continue;
            }

            if (equalsUnordered(source, entry.getValue().getAllElements())) {
                matches.add(entry.getKey());
            }
        }

        if (matches.size() > 1) {
            Log.debug("Ambiguous tag-reference match. Tags: {}, Blocks: {}", matches, toString(source));
            return null;
        }

        for (ITag.INamedTag<Block> named : TARGET_TAGS) {
            if (equalsUnordered(source, named.getAllElements())) {
                Log.debug("Matched tag-reference to named-tag. Tag: {}, Blocks: {}", named.getName(), toString(source));
                return named;
            }
        }

        return null;
    }

    private static boolean equalsUnordered(List<Block> source, List<Block> test) {
        return source.size() == test.size() && source.containsAll(test);
    }

    private static String toString(Collection<Block> collection) {
        StringBuilder sb = new StringBuilder(128).append('[');
        collection.stream().map(Block::getRegistryName)
                .filter(Objects::nonNull)
                .sorted()
                .forEach(s -> sb.append(sb.length() > 1 ? ", " : "").append(s));
        return sb.append(']').toString();
    }
}
