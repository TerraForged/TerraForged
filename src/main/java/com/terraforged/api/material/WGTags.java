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

package com.terraforged.api.material;

import com.terraforged.mod.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class WGTags {

    private static final Map<ITag<?>, Set<Block>> SETS = new ConcurrentHashMap<>();

    public static final ITag.INamedTag<Block> STONE = tag("forge:wg_stone");
    public static final ITag.INamedTag<Block> DIRT = tag("forge:wg_dirt");
    public static final ITag.INamedTag<Block> CLAY = tag("forge:wg_clay");
    public static final ITag.INamedTag<Block> SEDIMENT = tag("forge:wg_sediment");
    public static final ITag.INamedTag<Block> ERODIBLE = tag("forge:wg_erodible");
    public static final List<ITag.INamedTag<Block>> WG_TAGS = Collections.unmodifiableList(Arrays.asList(STONE, DIRT, CLAY, SEDIMENT, ERODIBLE));

    public static void init() {
    }

    private static ITag.INamedTag<Block> tag(String name) {
        ITag.INamedTag<Block> tag = BlockTags.makeWrapperTag(name);
        SETS.put(tag, new HashSet<>());
        return tag;
    }

    public static void set(ITag<Block> tag, Set<Block> set) {
        SETS.put(tag, set);
    }

    public static Set<Block> getSet(ITag<?> tag) {
        return SETS.getOrDefault(tag, Collections.emptySet());
    }

    public static Predicate<BlockState> stone() {
        return toStatePredicate(STONE);
    }

    private static Predicate<BlockState> toStatePredicate(ITag<Block> tag) {
        return state -> tag.contains(state.getBlock());
    }

    public static void printTags() {
        for (ITag.INamedTag<Block> tag : WG_TAGS) {
            Set<Block> set = new HashSet<>();
            Log.debug("World-Gen Tag: {}", tag.getName());
            for (Block block : tag.getAllElements()) {
                Log.debug(" - {}", block.getRegistryName());
                set.add(block);
            }
            SETS.put(tag, set);
        }
    }
}
