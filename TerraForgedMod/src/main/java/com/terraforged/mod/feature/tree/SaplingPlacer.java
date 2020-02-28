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

import com.terraforged.feature.template.decorator.DecoratedFeature;
import com.terraforged.feature.template.decorator.DecoratorWorld;
import com.terraforged.feature.template.feature.TemplateFeature;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;

public class SaplingPlacer {

    public static boolean placeTree(Feature<NoFeatureConfig> feature, SaplingGrowTreeEvent event, Vec3i[] dirs) {
        if (feature == null) {
            return false;
        }

        event.setResult(Event.Result.DENY);

        if (feature instanceof DecoratedFeature) {
            return placeDecorated((DecoratedFeature<?,?>) feature, event, dirs);
        }

        return placeNormal(feature, event, dirs);
    }

    private static <W extends DecoratorWorld> boolean placeDecorated(DecoratedFeature<?, W> feature, SaplingGrowTreeEvent event, Vec3i[] dirs) {
        if (!(event.getWorld().getChunkProvider() instanceof ServerChunkProvider)) {
            return false;
        }

        TreeBuffer buffer = new TreeBuffer(event.getWorld(), event.getPos());
        W world = feature.wrap(buffer);

        ChunkGenerator<?> generator = ((ServerChunkProvider) event.getWorld().getChunkProvider()).getChunkGenerator();
        feature.placeFeature(world, generator, event.getRand(), event.getPos(), NoFeatureConfig.NO_FEATURE_CONFIG);

        // check that the tree can grow here
        if (overheadIsSolid(event.getWorld(), event.getPos(), buffer.getTopY())) {
            return false;
        }

        BlockPos translation = buffer.getBaseMin().add(getMin(dirs));

        // apply buffer to world with translation
        applyBuffer(buffer, event.getWorld(), translation);

        // translate the decoration positions and apply in the world
        world.setDelegate(event.getWorld());
        world.translate(translation);
        feature.decorate(world, event.getRand());
        return true;
    }

    private static boolean placeNormal(Feature<NoFeatureConfig> feature, SaplingGrowTreeEvent event, Vec3i[] dirs) {
        // apply the feature to a buffer
        TreeBuffer buffer = new TreeBuffer(event.getWorld(), event.getPos());
        buffer.placeFeature(feature, event.getPos(), event.getRand());

        // check that the tree can grow here
        if (overheadIsSolid(event.getWorld(), event.getPos(), buffer.getTopY())) {
            return false;
        }

        // get the min position in the 2x2 grid
        BlockPos translation = buffer.getBaseMin().add(getMin(dirs));

        // copy the feature from the buffer to the world while translating each block
        applyBuffer(buffer, event.getWorld(), translation);
        return true;
    }

    private static void applyBuffer(TreeBuffer buffer, IWorld world, BlockPos translation) {
        try (BlockPos.PooledMutable pos = BlockPos.PooledMutable.retain()) {
            for (TemplateFeature.BlockInfo block : buffer.getChanges()) {
                int x = block.getPos().getX() + translation.getX();
                int y = block.getPos().getY();
                int z = block.getPos().getZ() + translation.getZ();

                pos.setPos(x, y, z);

                if (pos.getY() > 90) {
                    BlockState current = world.getBlockState(pos);
                    if (current.isSolid()) {
                        continue;
                    }
                }

                world.setBlockState(pos, block.getState(), 2);
            }
        }
    }

    private static boolean overheadIsSolid(IWorld world, BlockPos pos, int topY) {
        try (BlockPos.PooledMutable blockPos = BlockPos.PooledMutable.retain()) {
            for (int y = pos.getY(); y <= topY; y++) {
                blockPos.setPos(pos.getX(), y, pos.getZ());
                BlockState state = world.getBlockState(pos);
                if (state.isSolid()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static Vec3i getMin(Vec3i[] dirs) {
        Vec3i min = Vec3i.NULL_VECTOR;
        for (Vec3i dir : dirs) {
            if (dir.getX() < min.getX() && dir.getZ() <= min.getZ()) {
                min = dir;
                continue;
            }
            if (dir.getZ() < min.getZ() && dir.getX() <= min.getX()) {
                min = dir;
            }
        }
        return min;
    }
}
