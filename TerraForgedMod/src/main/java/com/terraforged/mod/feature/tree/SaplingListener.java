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

import com.terraforged.mod.TerraWorld;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SaplingListener {

    private static final BlockPos[] NONE = {BlockPos.ZERO};

    private static final Vec3i[][] DIRECTIONS = {
            {new Vec3i(0, 0, 1), new Vec3i(1, 0, 1), new Vec3i(1, 0, 0)},
            {new Vec3i(1, 0, 0), new Vec3i(1, 0, -1), new Vec3i(0, 0, -1)},
            {new Vec3i(0, 0, -1), new Vec3i(-1, 0, -1), new Vec3i(-1, 0, 0)},
            {new Vec3i(-1, 0, 0), new Vec3i(-1, 0, 1), new Vec3i(0, 0, 1)},
    };

    @SubscribeEvent
    public static void onTreeGrow(SaplingGrowTreeEvent event) {
        // ignore if client
        if (event.getWorld().isRemote()) {
            return;
        }
        // ignore other world types
        if (!TerraWorld.isTerraWorld(event.getWorld())) {
            return;
        }
        // has user disabled custom trees?
        if (!areCustomTreesEnabled(event.getWorld())) {
            return;
        }

        IWorld world = event.getWorld();
        BlockPos pos = event.getPos();
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof SaplingBlock) {
            // get the sapling feature for the given block type
            SaplingFeature tree = SaplingManager.getSapling(block.getRegistryName());

            // tree is null if the sapling type hasn't been configured
            if (tree == null) {
                return;
            }

            // check for a 2x2 arrangement of sapling blocks around the position
            Vec3i[] directions = getNeighbourDirections(world, block, pos);

            // length is 1 if a full 2x2 arrangement could not be found
            if (directions.length == 1) {
                placeNormal(tree, event, directions);
            } else {
                placeGiant(tree, event, block, directions);
            }
        }
    }

    private static void placeNormal(SaplingFeature tree, SaplingGrowTreeEvent event, Vec3i[] directions) {
        SaplingPlacer.placeTree(tree.nextNormal(event.getRand()), event, directions);
    }

    private static void placeGiant(SaplingFeature tree, SaplingGrowTreeEvent event, Block type, Vec3i[] directions) {
        Feature<NoFeatureConfig> feature = tree.nextGiant(event.getRand());

        // if no giant tree exists for this sapling type try and place a normal one instead
        if (feature == null) {
            placeNormal(tree, event, directions);
            return;
        }

        // do not continue if unable to place the tree
        if (!SaplingPlacer.placeTree(feature, event, directions)) {
            return;
        }

        // iterate through the contributing saplings and remove any that were not destroyed during tree creation
        BlockPos pos = event.getPos();
        for (Vec3i dir : directions) {
            BlockPos blockPos = pos.add(dir);
            BlockState state = event.getWorld().getBlockState(blockPos);
            if (state.getBlock() == type) {
                event.getWorld().removeBlock(blockPos, false);
            }
        }
    }

    private static Vec3i[] getNeighbourDirections(IWorld world, Block block, BlockPos pos) {
        for (Vec3i[] dirs : DIRECTIONS) {
            boolean match = true;
            for (Vec3i dir : dirs) {
                BlockState state = world.getBlockState(pos.add(dir));
                if (state.getBlock() != block) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return dirs;
            }
        }
        return NONE;
    }

    private static boolean areCustomTreesEnabled(IWorld world) {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            ChunkGenerator<?> generator = serverWorld.getChunkProvider().generator;
            if (generator instanceof TerraChunkGenerator) {
                TerraContext context = ((TerraChunkGenerator) generator).getContext();
                return context.terraSettings.features.customBiomeFeatures;
            }
        }
        return false;
    }
}
