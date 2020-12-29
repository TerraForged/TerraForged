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

package com.terraforged.mod.featuremanager.template.type.tree;

import com.terraforged.mod.featuremanager.template.feature.Placement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.IWorldGenerationBaseReader;
import net.minecraft.world.gen.feature.TreeFeature;

public class TreePlacement implements Placement {

    public static final Placement PLACEMENT = new TreePlacement();

    private TreePlacement() {

    }

    @Override
    public boolean canPlaceAt(IWorld world, BlockPos pos) {
        return isSoil(world, pos.down()) && clearOverhead(world, pos);
    }

    @Override
    public boolean canReplaceAt(IWorld world, BlockPos pos) {
        return TreeFeature.isReplaceableAt(world, pos);
    }

    private static boolean isSoil(IWorld world, BlockPos pos) {
        return TreeFeature.isDirt(world.getBlockState(pos).getBlock());
    }

    private static boolean isReplaceableAt(IWorld world, BlockPos pos) {
        return TreeFeature.isReplaceableAt(world, pos);
    }

    private static boolean clearOverhead(IWorld world, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        // world.getMaxHeight ?
        int max = Math.min(world.func_234938_ad_(), pos.getY() + 20);
        for (int y = pos.getY(); y < max; y++) {
            mutable.setPos(pos.getX(), y, pos.getZ());
            if (!isReplaceableAt(world, mutable)) {
                return false;
            }
        }
        return true;
    }
}
