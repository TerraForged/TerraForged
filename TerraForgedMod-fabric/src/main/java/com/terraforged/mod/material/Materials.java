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

import com.terraforged.api.material.MaterialTags;
import com.terraforged.api.material.layer.LayerManager;
import com.terraforged.api.material.state.States;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tag.Tag;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class Materials {

    private final Set<Block> stone = create(MaterialTags.WG_ROCK);
    private final Set<Block> dirt = create(MaterialTags.WG_EARTH);
    private final Set<Block> clay = create(MaterialTags.WG_CLAY);
    private final Set<Block> sediment = create(MaterialTags.WG_SEDIMENT);
    private final Set<Block> ore = create(MaterialTags.WG_ORE);
    private final Set<Block> erodible = create(MaterialTags.WG_ERODIBLE);
    private final LayerManager layerManager = new LayerManager();

    public Materials() {
        Predicate<Block> filter = getTagFilter();
        for (Block block : Registry.BLOCK) {
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
            } else if (MaterialHelper.isOre(block)) {
                ore.add(block);
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

    public boolean isOre(Block block) {
        return ore.contains(block);
    }

    public boolean isErodible(Block block) {
        return erodible.contains(block);
    }

    public Collection<Block> getStone() {
        if (stone.isEmpty()) {
            return Collections.singleton(States.STONE.getBlock());
        }
        return Collections.unmodifiableSet(stone);
    }

    public Collection<Block> getDirt() {
        if (dirt.isEmpty()) {
            return Collections.singleton(States.DIRT.getBlock());
        }
        return Collections.unmodifiableSet(dirt);
    }

    public Collection<Block> getClay() {
        if (clay.isEmpty()) {
            return Collections.singleton(States.STONE.getBlock());
        }
        return Collections.unmodifiableSet(clay);
    }

    public Collection<Block> getSediment() {
        if (sediment.isEmpty()) {
            return Collections.singleton(States.CLAY.getBlock());
        }
        return Collections.unmodifiableSet(sediment);
    }

    public Collection<Block> getOre() {
        if (ore.isEmpty()) {
            return Collections.singleton(States.STONE.getBlock());
        }
        return Collections.unmodifiableSet(ore);
    }

    private static Set<Block> create(Tag<Block> tag) {
        return new HashSet<>(tag.values());
    }

    private static Predicate<Block> getTagFilter() {
        Set<String> namespaces = new HashSet<>();
        collectNamespace(namespaces, MaterialTags.WG_ROCK.values());
        collectNamespace(namespaces, MaterialTags.WG_EARTH.values());
        collectNamespace(namespaces, MaterialTags.WG_EARTH.values());
        collectNamespace(namespaces, MaterialTags.WG_SEDIMENT.values());
        collectNamespace(namespaces, MaterialTags.WG_ORE.values());
        return b -> namespaces.contains(MaterialHelper.getNamespace(Registry.BLOCK, b));
    }

    private static void collectNamespace(Set<String> set, Collection<Block> blocks) {
        for (Block block : blocks) {
            set.add(MaterialHelper.getNamespace(Registry.BLOCK, block));
        }
    }
}
