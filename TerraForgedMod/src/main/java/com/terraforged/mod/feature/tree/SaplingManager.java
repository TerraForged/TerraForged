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

package com.terraforged.mod.feature.tree;

import com.mojang.datafixers.types.DynamicOps;
import com.terraforged.mod.Log;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SaplingManager {

    private static final Map<ResourceLocation, SaplingFeature> saplings = new HashMap<>();

    public static SaplingFeature getSapling(ResourceLocation name) {
        return saplings.get(name);
    }

    public static void register(Block block, SaplingConfig config) {
        register(block.getRegistryName(), config);
    }

    public static <T> void register(ResourceLocation location, T config, DynamicOps<T> ops) {
        register(location, new SaplingConfig(config, ops));
    }

    public static void register(ResourceLocation location, SaplingConfig config) {
        saplings.put(location, new SaplingFeature(config));
    }

    public static void init() {
        register(Blocks.OAK_SAPLING, new SaplingConfig()
                .addNormal("terraforged:oak_small", 4)
                .addNormal("terraforged:oak_forest", 3)
                .addNormal("terraforged:oak_large", 2)
                .addGiant("terraforged:oak_huge", 1));

        register(Blocks.BIRCH_SAPLING, new SaplingConfig()
                .addNormal("terraforged:birch_small", 4)
                .addNormal("terraforged:birch_forest", 3)
                .addNormal("terraforged:birch_large", 1));

        register(Blocks.JUNGLE_SAPLING, new SaplingConfig()
                .addNormal("terraforged:jungle_small", 4)
                .addGiant("terraforged:jungle_large", 1));

        register(
                Blocks.SPRUCE_SAPLING,
                new SaplingConfig()
                        .addNormal("terraforged:spruce_small", 4)
                        .addNormal("terraforged:spruce_large", 1)
                        .addGiant("terraforged:redwood_huge", 1));

        register(Blocks.DARK_OAK_SAPLING, new SaplingConfig()
                .addNormal("terraforged:dark_oak_small", 4)
                .addNormal("terraforged:dark_oak_large", 1));

        register(Blocks.ACACIA_SAPLING, new SaplingConfig()
                .addNormal("terraforged:acacia_small", 2)
                .addNormal("terraforged:acacia_large", 1));

        Log.info("Registered saplings");
    }
}
