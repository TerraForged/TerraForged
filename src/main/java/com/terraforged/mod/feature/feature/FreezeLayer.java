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

package com.terraforged.mod.feature.feature;

import com.terraforged.fm.template.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

public class FreezeLayer extends Feature<NoFeatureConfig> {

    public static final FreezeLayer INSTANCE = new FreezeLayer();

    public FreezeLayer() {
        super(NoFeatureConfig.field_236558_a_);
        setRegistryName("terraforged", "freeze_top_layer");
    }

    @Override
    public boolean func_241855_a(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        BlockPos.Mutable pos1 = new BlockPos.Mutable();
        BlockPos.Mutable pos2 = new BlockPos.Mutable();

        for(int dx = -16; dx < 32; ++dx) {
            for(int dz = -16; dz < 32; ++dz) {
                int x = pos.getX() + dx;
                int z = pos.getZ() + dz;
                int leavesY = world.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z);
                int groundY = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

                pos1.setPos(x, leavesY, z);
                pos2.setPos(pos1).move(Direction.DOWN, 1);
                Biome biome = world.getBiome(pos1);

                if (leavesY > groundY) {
                    freezeLeaves(world, biome, pos1, pos2);
                }

                if (dx > -1 && dx < 16 && dz > -1 && dz < 16) {
                    pos1.setPos(x, groundY, z);
                    pos2.setPos(pos1).move(Direction.DOWN, 1);
                    freezeGround(world, biome, pos1, pos2);
                }
            }
        }

        return true;
    }

    private void freezeLeaves(IWorld world, Biome biome, BlockPos.Mutable pos, BlockPos.Mutable below) {
        if (biome.doesSnowGenerate(world, pos)) {
            BlockState stateUnder = world.getBlockState(below);
            if (stateUnder.getBlock() == Blocks.AIR) {
                return;
            }
            setSnow(world, pos, below, stateUnder);
        }
    }

    private void freezeGround(IWorld world, Biome biome, BlockPos.Mutable pos, BlockPos.Mutable below) {
        if (biome.doesWaterFreeze(world, below, false)) {
            world.setBlockState(below, Blocks.ICE.getDefaultState(), 2);
        }

        if (biome.doesSnowGenerate(world, pos)) {
            BlockState stateUnder = world.getBlockState(below);

            if (stateUnder.getBlock() == Blocks.AIR) {
                return;
            }

            if (BlockTags.LOGS.contains(stateUnder.getBlock())) {
                return;
            }

            pos.move(Direction.UP, 1);
            BlockState above = world.getBlockState(pos);
            if (BlockTags.LOGS.contains(above.getBlock()) || BlockTags.LEAVES.contains(above.getBlock())) {
                return;
            }

            pos.move(Direction.DOWN, 1);
            if (setSnow(world, pos, below, stateUnder)) {
                if (above.getBlock() != Blocks.AIR) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                }
            }
        }
    }

    private boolean setSnow(IWorld world, BlockPos pos1, BlockPos pos2, BlockState below) {
        if (BlockUtils.isSolid(world, pos1)) {
            return false;
        }

        world.setBlockState(pos1, Blocks.SNOW.getDefaultState(), 2);

        if (below.hasProperty(SnowyDirtBlock.SNOWY)) {
            world.setBlockState(pos2, below.with(SnowyDirtBlock.SNOWY, true), 2);
        }
        return true;
    }
}
