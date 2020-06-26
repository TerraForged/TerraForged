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

package com.terraforged.mod.feature.sapling;

import com.terraforged.mod.TerraWorld;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.feature.BlockDataManager;
import com.terraforged.fm.template.Template;
import com.terraforged.fm.template.feature.TemplateFeature;
import com.terraforged.fm.template.feature.TemplateFeatureConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SaplingListener {

    private static final BlockPos[] CENTER = {BlockPos.ZERO};

    private static final Vec3i[][] DIRECTIONS = {
            {new Vec3i(0, 0, 1), new Vec3i(1, 0, 1), new Vec3i(1, 0, 0)},
            {new Vec3i(1, 0, 0), new Vec3i(1, 0, -1), new Vec3i(0, 0, -1)},
            {new Vec3i(0, 0, -1), new Vec3i(-1, 0, -1), new Vec3i(-1, 0, 0)},
            {new Vec3i(-1, 0, 0), new Vec3i(-1, 0, 1), new Vec3i(0, 0, 1)},
    };

    @SubscribeEvent
    public static void onTreeGrow(SaplingGrowTreeEvent event) {
        Optional<BlockDataManager> dataManager = getDataManger(event);
        if (!dataManager.isPresent()) {
            return;
        }

        IWorld world = event.getWorld();
        Block block = world.getBlockState(event.getPos()).getBlock();
        Optional<SaplingConfig> saplingConfig = dataManager.get().getConfig(block, SaplingConfig.class);
        if (!saplingConfig.isPresent()) {
            return;
        }

        Mirror mirror = TemplateFeature.nextMirror(event.getRand());
        Rotation rotation = TemplateFeature.nextRotation(event.getRand());

        // if part of a 2x2 grid get the min corner of it
        BlockPos pos = getMinPos(world, block, event.getPos(), saplingConfig.get().hasGiant());
        Vec3i[] directions = getNeighbours(world, block, pos, saplingConfig.get().hasGiant());
        TemplateFeatureConfig feature = saplingConfig.get().next(event.getRand(), directions.length == 3);
        if (feature == null) {
            return;
        }

        // translate the pos so that the mirrored/rotated 2x2 grid aligns correctly
        BlockPos origin = pos.subtract(getTranslation(directions, mirror, rotation));

        // require that directly above the sapling(s) is open air
        if (!isClearOverhead(world, origin, directions)) {
            event.setResult(Event.Result.DENY);
            return;
        }

        // attempt to paste the tree & then clear up any remaining saplings
        if (TemplateFeature.pasteChecked(world, event.getRand(), origin, mirror, rotation, feature, feature.decorator)) {
            event.setResult(Event.Result.DENY);
            for (Vec3i dir : directions) {
                BlockPos neighbour = origin.add(dir);
                BlockState state = world.getBlockState(neighbour);
                if (state.getBlock() == block) {
                    world.destroyBlock(neighbour, false);
                }
            }
        }
    }

    private static Optional<BlockDataManager> getDataManger(SaplingGrowTreeEvent event) {
        // ignore if client
        if (event.getWorld().isRemote()) {
            return Optional.empty();
        }

        // ignore other world types
        if (!TerraWorld.isTerraWorld(event.getWorld())) {
            return Optional.empty();
        }

        // get the data manager from the world's chunk generator
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();
            ChunkGenerator<?> generator = serverWorld.getChunkProvider().generator;
            if (generator instanceof TerraChunkGenerator) {
                TerraContext context = ((TerraChunkGenerator) generator).getContext();
                if (context.terraSettings.miscellaneous.customBiomeFeatures) {
                    return Optional.of(((TerraChunkGenerator) generator).getBlockDataManager());
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isClearOverhead(IWorld world, BlockPos pos, Vec3i[] directions) {
        for (Vec3i dir : directions) {
            int x = pos.getX() + dir.getX();
            int z = pos.getZ() + dir.getZ();
            int y = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y > pos.getY()) {
                return false;
            }
        }
        return true;
    }

    private static BlockPos getMinPos(IWorld world, Block block, BlockPos pos, boolean checkNeighbours) {
        if (checkNeighbours) {
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
                    return pos.add(getMin(dirs, Mirror.NONE, Rotation.NONE));
                }
            }
        }
        return pos;
    }

    private static Vec3i getTranslation(Vec3i[] directions, Mirror mirror, Rotation rotation) {
        if (directions.length == 1 || mirror == Mirror.NONE && rotation == Rotation.NONE) {
            return Vec3i.NULL_VECTOR;
        }
        return getMin(directions, mirror, rotation);
    }

    private static Vec3i getMin(Vec3i[] directions, Mirror mirror, Rotation rotation) {
        Vec3i min = Vec3i.NULL_VECTOR;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (Vec3i vec : directions) {
            BlockPos dir = Template.transform(pos.setPos(vec), mirror, rotation);
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

    private static Vec3i[] getNeighbours(IWorld world, Block block, BlockPos pos, boolean checkNeighbours) {
        if (checkNeighbours) {
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
        }
        return CENTER;
    }
}
