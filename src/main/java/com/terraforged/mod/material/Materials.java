/*
 *
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

package com.terraforged.mod.material;

import com.terraforged.api.material.WGTags;
import com.terraforged.api.material.layer.LayerManager;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.Tag;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Deprecated
public class Materials {

    private final Set<Block> stone = create(WGTags.STONE);
    private final Set<Block> dirt = create(WGTags.DIRT);
    private final Set<Block> clay = create(WGTags.CLAY);
    private final Set<Block> sediment = create(WGTags.SEDIMENT);
    private final Set<Block> erodible = create(WGTags.ERODIBLE);
    private final LayerManager layerManager = new LayerManager();

    public Materials() {
        Predicate<Block> filter = getTagFilter();
        for (Block block : ForgeRegistries.BLOCKS) {
            if (filter.test(block)) {
                continue;
            }

            if (!MaterialHelper.isCube(block.getDefaultState())) {
                continue;
            }

            if (MaterialHelper.isStone(block)) {
                stone.add(block);
            } else if (MaterialHelper.isDirt(block)) {
                dirt.add(block);
            } else if (MaterialHelper.isClay(block)) {
                clay.add(block);
            } else if (MaterialHelper.isSediment(block)) {
                sediment.add(block);
            }
        }

        if (stone.isEmpty()) {
            stone.add(Blocks.STONE);
        }
    }

    public LayerManager getLayerManager() {
        return layerManager;
    }

    public boolean isStone(Block block) {
        return stone.contains(block);
    }

    public boolean isEarth(Block block) {
        return dirt.contains(block);
    }

    public boolean isClay(Block block) {
        return clay.contains(block);
    }

    public boolean isSediment(Block block) {
        return sediment.contains(block);
    }

    public boolean isErodible(Block block) {
        return erodible.contains(block);
    }

    private static Set<Block> create(Tag<Block> tag) {
        return new HashSet<>(tag.getAllElements());
    }

    private static Predicate<Block> getTagFilter() {
        Set<String> namespaces = new HashSet<>();
        collectNamespace(namespaces, WGTags.STONE.getAllElements());
        collectNamespace(namespaces, WGTags.DIRT.getAllElements());
        collectNamespace(namespaces, WGTags.DIRT.getAllElements());
        collectNamespace(namespaces, WGTags.SEDIMENT.getAllElements());
        return b -> namespaces.contains(MaterialHelper.getNamespace(b));
    }

    private static void collectNamespace(Set<String> set, Collection<Block> blocks) {
        for (Block block : blocks) {
            set.add(MaterialHelper.getNamespace(block));
        }
    }
}
