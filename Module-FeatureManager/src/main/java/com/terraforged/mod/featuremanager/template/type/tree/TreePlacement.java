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

import com.terraforged.mod.featuremanager.template.BlockUtils;
import com.terraforged.mod.featuremanager.template.feature.Placement;
import com.terraforged.mod.featuremanager.template.template.Dimensions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.function.BiPredicate;

public class TreePlacement implements Placement {

    public static final Placement PLACEMENT = new TreePlacement();
    private static final BiPredicate<IWorld, BlockPos> OVERHEAD = BlockUtils::canTreeReplace;

    private TreePlacement() {

    }

    @Override
    public boolean canPlaceAt(IWorld world, BlockPos pos, Dimensions dimensions) {
        return BlockUtils.isSoil(world, pos.down())
                && BlockUtils.isClearOverhead(world, pos, dimensions.getSizeY(), TreePlacement.OVERHEAD);
    }

    @Override
    public boolean canReplaceAt(IWorld world, BlockPos pos) {
        return BlockUtils.canTreeReplace(world, pos);
    }
}
