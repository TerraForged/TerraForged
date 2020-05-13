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

import com.google.common.collect.Sets;
import com.terraforged.core.concurrent.ObjectPool;
import com.terraforged.mod.util.DummyBlockReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.material.Material;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Set;

public class MaterialHelper {

    private static final Set<Block> BLACKLIST = Sets.newHashSet(
            Blocks.INFESTED_CHISELED_STONE_BRICKS,
            Blocks.INFESTED_COBBLESTONE,
            Blocks.INFESTED_CRACKED_STONE_BRICKS,
            Blocks.INFESTED_MOSSY_STONE_BRICKS,
            Blocks.INFESTED_STONE,
            Blocks.INFESTED_STONE_BRICKS,
            Blocks.SLIME_BLOCK,
            Blocks.RED_SAND,
            Blocks.SOUL_SAND,
            // honey etc
            Blocks.HONEY_BLOCK,
            Blocks.HONEYCOMB_BLOCK,
            Blocks.BEE_NEST,
            Blocks.BEEHIVE,
            Blocks.COMPOSTER
    );

    public static boolean isAir(Block block) {
        return block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR;
    }

    public static boolean isGrass(Block block) {
        return block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM;
    }

    public static boolean isStone(Block block) {
        return Tags.Blocks.STONE.contains(block)
                && !isBlacklisted(block)
                && !("" + block.getRegistryName()).contains("polished_");
    }

    public static boolean isDirt(Block block) {
        return Tags.Blocks.DIRT.contains(block)
                && !isBlacklisted(block);
    }

    public static boolean isClay(Block block) {
        return block.getDefaultState().getMaterial() == Material.CLAY
                && !isBlacklisted(block);
    }

    public static boolean isSand(Block block) {
        return BlockTags.SAND.contains(block)
                && !isBlacklisted(block)
                && !(block instanceof ConcretePowderBlock);
    }

    public static boolean isSediment(Block block) {
        return (isSand(block) || isGravel(block))
                && !isBlacklisted(block)
                && !(block instanceof ConcretePowderBlock);
    }

    public static boolean isGravel(Block block) {
        return getName(block).contains("gravel");
    }

    public static boolean isOre(Block block) {
        return Tags.Blocks.ORES.contains(block)
                && !isBlacklisted(block);
    }

    public static boolean isBlacklisted(Block block) {
        return BLACKLIST.contains(block);
    }

    public static String getName(IForgeRegistryEntry<?> entry) {
        return "" + entry.getRegistryName();
    }

    public static String getNamespace(IForgeRegistryEntry<?> entry) {
        ResourceLocation name = entry.getRegistryName();
        if (name == null) {
            return "unknown";
        }
        return name.getNamespace();
    }

    public static float getHardness(BlockState state) {
        try (ObjectPool.Item<DummyBlockReader> reader = DummyBlockReader.pooled()) {
            reader.getValue().set(state);
            return state.getBlockHardness(reader.getValue(), BlockPos.ZERO);
        }
    }

    public static boolean isCube(BlockState state) {
        try (ObjectPool.Item<DummyBlockReader> reader = DummyBlockReader.pooled()) {
            reader.getValue().set(state);
            return state.isNormalCube(reader.getValue(), BlockPos.ZERO);
        }
    }

    public static OreFeatureConfig getOreConfig(ConfiguredFeature<?, ?> feature) {
        if (feature.config instanceof DecoratedFeatureConfig) {
            DecoratedFeatureConfig config = (DecoratedFeatureConfig) feature.config;
            if (config.feature.config instanceof OreFeatureConfig) {
                return (OreFeatureConfig) config.feature.config;
            }
        }
        return null;
    }
}
