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

package com.terraforged.mod.featuremanager.template;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.TreeFeature;

import java.util.function.BiPredicate;

public class BlockUtils {

    public static boolean isSoil(IWorld world, BlockPos pos) {
        return TreeFeature.isDirt(world.getBlockState(pos).getBlock());
    }

    public static boolean isVegetation(IWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isIn(BlockTags.SAPLINGS) || state.isIn(BlockTags.FLOWERS) || state.isIn(Blocks.VINE.delegate.get());
    }

    public static boolean canTreeReplace(IWorld world, BlockPos pos) {
        return TreeFeature.isReplaceableAt(world, pos) || isVegetation(world, pos);
    }

    public static boolean isSolid(IWorldReader reader, BlockPos pos) {
        BlockState state = reader.getBlockState(pos);
        return isSolid(state, reader, pos);
    }

    public static boolean isSolid(BlockState state, IWorldReader reader, BlockPos pos) {
        return state.isSolid() || !state.allowsMovement(reader, pos, PathType.LAND);
    }

    public static boolean isSolidNoIce(IWorldReader reader, BlockPos pos) {
        BlockState state = reader.getBlockState(pos);
        return isSolid(state, reader, pos) && !BlockTags.ICE.contains(state.getBlock());
    }

    public static boolean isClearOverhead(IWorld world, BlockPos pos, int height, BiPredicate<IWorld, BlockPos> predicate) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        // world.getMaxHeight ?
        int max = Math.min(world.func_234938_ad_() - 1, pos.getY() + height);
        for (int y = pos.getY(); y < max; y++) {
            mutable.setPos(pos.getX(), y, pos.getZ());
            if (!predicate.test(world, mutable)) {
                return false;
            }
        }
        return true;
    }
}
