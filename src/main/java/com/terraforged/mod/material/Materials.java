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

package com.terraforged.mod.material;

import com.terraforged.mod.api.material.WGTags;
import com.terraforged.mod.api.material.layer.LayerManager;
import com.terraforged.mod.api.material.state.States;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.mod.chunk.util.DummyBlockReader;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.block.*;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;

public class Materials {

    private static final Comparator<IForgeRegistryEntry<?>> COMPARATOR = Comparator.comparing(IForgeRegistryEntry::getRegistryName);

    public final LayerManager layerManager = new LayerManager();
    public final Set<Block> stone = create(WGTags.STONE, States.STONE.getBlock());
    public final Set<Block> dirt = create(WGTags.DIRT, States.DIRT.getBlock());
    public final Set<Block> clay = create(WGTags.CLAY, States.CLAY.getBlock());
    public final Set<Block> sediment = create(WGTags.SEDIMENT, States.GRAVEL.getBlock());
    public final Set<Block> erodible = create(WGTags.ERODIBLE, null);

    private Materials() {

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

    public boolean isAir(Block block) {
        return block instanceof AirBlock;
    }

    public boolean isGrass(Block block) {
        return block instanceof GrassBlock || block instanceof MyceliumBlock;
    }

    private static Set<Block> create(ITag<Block> tag, Block def) {
        try {
            ObjectOpenHashSet<Block> set = new ObjectOpenHashSet<>(tag.getAllElements());
            if (set.isEmpty() && def != null) {
                set.add(def);
            }
            return ObjectSets.unmodifiable(set);
        } catch (Throwable t) {
            return Collections.singleton(def);
        }
    }

    public static float getHardness(BlockState state) {
        try (Resource<DummyBlockReader> reader = DummyBlockReader.pooled()) {
            reader.get().set(state);
            return state.getBlockHardness(reader.get(), BlockPos.ZERO);
        }
    }

    public static <T extends IForgeRegistryEntry<T>> List<T> toList(Collection<T> collection) {
        List<T> list = new ArrayList<>(collection);
        list.sort(Materials.COMPARATOR);
        return Collections.unmodifiableList(list);
    }

    public static Materials create() {
        return new Materials();
    }
}
