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

package com.terraforged.mod.featuremanager.template.template;

import com.terraforged.mod.featuremanager.template.PasteConfig;
import com.terraforged.mod.featuremanager.template.StructureUtils;
import com.terraforged.mod.featuremanager.template.buffer.BufferIterator;
import com.terraforged.mod.featuremanager.template.buffer.PasteBuffer;
import com.terraforged.mod.featuremanager.template.buffer.TemplateBuffer;
import com.terraforged.mod.featuremanager.template.feature.Placement;
import com.terraforged.mod.featuremanager.util.BlockReader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Template {

    private static final int PASTE_FLAG = 3 | 16;
    private static final Direction[] directions = Direction.values();
    private static final ThreadLocal<PasteBuffer> PASTE_BUFFER = ThreadLocal.withInitial(PasteBuffer::new);
    private static final ThreadLocal<TemplateBuffer> TEMPLATE_BUFFER = ThreadLocal.withInitial(TemplateBuffer::new);

    private final BakedTemplate template;
    private final BakedDimensions dimensions;

    public Template(List<BlockInfo> blocks) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        BlockInfo[] blockArray = blocks.toArray(new BlockInfo[0]);

        for (int i = 0; i < blocks.size(); i++) {
            BlockInfo block = blocks.get(i);
            minX = Math.min(minX, block.pos.getX());
            minY = Math.min(minY, block.pos.getY());
            minZ = Math.min(minZ, block.pos.getZ());
            maxX = Math.max(maxX, block.pos.getX());
            maxY = Math.max(maxY, block.pos.getY());
            maxZ = Math.max(maxZ, block.pos.getZ());
            blockArray[i] = block;
        }

        Dimensions dimensions = new Dimensions(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
        this.template = new BakedTemplate(blockArray);
        this.dimensions = new BakedDimensions(dimensions);
    }

    public boolean paste(IWorld world, BlockPos origin, Mirror mirror, Rotation rotation, Placement placement, PasteConfig config) {
        if (config.checkBounds) {
            IChunk chunk = world.getChunk(origin);
            if (StructureUtils.hasOvergroundStructure(chunk)) {
                return pasteWithBoundsCheck(world, origin, mirror, rotation, placement, config);
            }
        }
        return pasteNormal(world, origin, mirror, rotation, placement, config);
    }

    public boolean pasteNormal(IWorld world, BlockPos origin, Mirror mirror, Rotation rotation, Placement placement, PasteConfig config) {
        boolean placed = false;
        BlockReader reader = new BlockReader();
        PasteBuffer buffer = PASTE_BUFFER.get();
        buffer.setRecording(config.updatePostPaste);

        BlockPos.Mutable pos1 = new BlockPos.Mutable();
        BlockPos.Mutable pos2 = new BlockPos.Mutable();

        BlockInfo[] blocks = template.get(mirror, rotation);
        for (int i = 0; i < blocks.length; i++) {
            BlockInfo block = blocks[i];
            addPos(pos1, origin, block.pos);

            // make sure we don't leak outside the region
            if (!hasChunk(pos1, world)) {
                continue;
            }

            // ignore air in the template
            if (!config.pasteAir && block.state.getBlock() == Blocks.AIR) {
                continue;
            }

            // don't replace existing solids
            if (!config.replaceSolid && !placement.canReplaceAt(world, pos1)) {
                continue;
            }

            // generate a base going downwards if necessary
            if (block.pos.getY() <= 0 && block.state.isNormalCube(reader.setState(block.state), BlockPos.ZERO)) {
                placeBase(world, pos1, pos2, block.state, config.baseDepth);
            }

            world.setBlockState(pos1, block.state, 2);
            buffer.record(i);

            placed = true;
        }

        if (config.updatePostPaste) {
            // once all blocks placed, iterate them and update neighbours if required
            buffer.reset();
            updatePostPlacement(world, buffer, blocks, origin, pos1, pos2);
        }

        return placed;
    }

    public boolean pasteWithBoundsCheck(IWorld world, BlockPos origin, Mirror mirror, Rotation rotation, Placement placement, PasteConfig config) {
        Dimensions dimensions = this.dimensions.get(mirror, rotation);
        TemplateBuffer buffer = TEMPLATE_BUFFER.get().init(world, origin, dimensions.min, dimensions.max);

        BlockPos.Mutable pos1 = new BlockPos.Mutable();
        BlockPos.Mutable pos2 = new BlockPos.Mutable();
        BlockInfo[] blocks = template.get(mirror, rotation);

        // record valid BlockInfos into the buffer
        for (int i = 0; i < blocks.length; i++) {
            BlockInfo block = blocks[i];
            addPos(pos1, origin, block.pos);

            // make sure we don't leak outside the region
            if (!hasChunk(pos1, world)) {
                continue;
            }

            buffer.record(i, block, pos1, placement, config);
        }

        boolean placed = false;
        BlockReader reader = new BlockReader();
        while (buffer.next()) {
            int i = buffer.nextIndex();
            BlockInfo block = blocks[i];
            addPos(pos1, origin, block.pos);

            if (pos1.getY() <= origin.getY() && block.state.isNormalCube(reader.setState(block.state), BlockPos.ZERO)) {
                placeBase(world, pos1, pos2, block.state, config.baseDepth);
                world.setBlockState(pos1, block.state, 2);
                placed = true;
            } else if (buffer.test(pos1)) {
                // test uses the placement mask to prevent blocks overriding existing
                // solid blocks
                placed = true;
                world.setBlockState(pos1, block.state, 2);
            } else {
                // if failed to place mark the pos as invalid
                buffer.exclude(i);
            }
        }

        if (config.updatePostPaste) {
            // once all blocks placed, iterate them and update neighbours if required
            buffer.reset();
            updatePostPlacement(world, buffer, blocks, origin, pos1, pos2);
        }

        return placed;
    }

    private static void updatePostPlacement(IWorld world, BufferIterator iterator, BlockInfo[] blocks, BlockPos origin, BlockPos.Mutable pos1, BlockPos.Mutable pos2) {
        if (!iterator.isEmpty()) {
            while (iterator.next()) {
                int index = iterator.nextIndex();
                if (index < 0 || index >= blocks.length) {
                    continue;
                }

                BlockInfo block = blocks[index];
                addPos(pos1, origin, block.pos);

                for (Direction direction : directions) {
                    updatePostPlacement(world, pos1, pos2, direction);
                }
            }
        }
    }

    private static void updatePostPlacement(IWorld world, BlockPos.Mutable pos1, BlockPos.Mutable pos2, Direction direction) {
        pos2.setPos(pos1).move(direction, 1);

        BlockState state1 = world.getBlockState(pos1);
        BlockState state2 = world.getBlockState(pos2);

        // update state at pos1 - the input position
        BlockState result1 = state1.updatePostPlacement(direction, state2, world, pos1, pos2);
        if (result1 != state1) {
            world.setBlockState(pos1, result1, PASTE_FLAG);
        }

        // update state at pos2 - the neighbour
        BlockState result2 = state2.updatePostPlacement(direction.getOpposite(), result1, world, pos2, pos1);
        if (result2 != state2) {
            world.setBlockState(pos2, result2, PASTE_FLAG);
        }
    }

    private void placeBase(IWorld world, BlockPos pos, BlockPos.Mutable pos2, BlockState state, int depth) {
        for (int dy = 0; dy < depth; dy++) {
            pos2.setPos(pos).move(Direction.DOWN, dy);
            if (world.getBlockState(pos2).isSolid()) {
                return;
            }
            world.setBlockState(pos2, state, 2);
        }
    }

    private static boolean hasChunk(BlockPos pos, IWorld world){
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        return world.chunkExists(cx, cz);
    }

    public static BlockPos transform(BlockPos pos, Mirror mirror, Rotation rotation) {
        return net.minecraft.world.gen.feature.template.Template.getTransformedPos(pos, mirror, rotation, BlockPos.ZERO);
    }

    public static void addPos(BlockPos.Mutable pos, BlockPos a, BlockPos b) {
        pos.setX(a.getX() + b.getX());
        pos.setY(a.getY() + b.getY());
        pos.setZ(a.getZ() + b.getZ());
    }

    public static Optional<Template> load(InputStream data) {
        try {
            CompoundNBT root = CompressedStreamTools.readCompressed(data);
            if (!root.contains("palette") || !root.contains("blocks")) {
                return Optional.empty();
            }
            BlockState[] palette = readPalette(root.getList("palette", Constants.NBT.TAG_COMPOUND));
            BlockInfo[] blockInfos = readBlocks(root.getList("blocks", Constants.NBT.TAG_COMPOUND), palette);
            List<BlockInfo> blocks = relativize(blockInfos);
            return Optional.of(new Template(blocks));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static BlockState[] readPalette(ListNBT list) {
        BlockState[] palette = new BlockState[list.size()];
        for (int i = 0; i < list.size(); i++) {
            try {
                palette[i] = NBTUtil.readBlockState(list.getCompound(i));
            } catch (Throwable t) {
                palette[i] = Blocks.AIR.getDefaultState();
            }
        }
        return palette;
    }

    private static BlockInfo[] readBlocks(ListNBT list, BlockState[] palette) {
        BlockInfo[] blocks = new BlockInfo[list.size()];
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT compound = list.getCompound(i);
            BlockState state = palette[compound.getInt("state")];
            BlockPos pos = readPos(compound.getList("pos", Constants.NBT.TAG_INT));
            blocks[i] = new BlockInfo(pos, state);
        }
        return blocks;
    }

    private static List<BlockInfo> relativize(BlockInfo[] blocks) {
        // find the lowest, most-central block (the origin)
        BlockPos origin = null;
        int lowestSolid = Integer.MAX_VALUE;

        for (BlockInfo block : blocks) {
            if (!block.getState().isSolid()) {
                continue;
            }

            if (origin == null) {
                origin = block.pos;
                lowestSolid = block.pos.getY();
            } else if (block.pos.getY() < lowestSolid) {
                origin = block.pos;
                lowestSolid = block.pos.getY();
            } else if (block.pos.getY() == lowestSolid) {
                if (block.pos.getX() < origin.getX() && block.pos.getZ() <= origin.getZ()) {
                    origin = block.pos;
                    lowestSolid = block.pos.getY();
                } else if (block.pos.getZ() < origin.getZ() && block.pos.getX() <= origin.getX()) {
                    origin = block.pos;
                    lowestSolid = block.pos.getY();
                }
            }
        }

        if (origin == null) {
            return Arrays.asList(blocks);
        }

        // relativize all blocks to the origin
        List<BlockInfo> list = new ArrayList<>(blocks.length);
        for (BlockInfo in : blocks) {
            BlockPos pos = in.pos.subtract(origin);
            list.add(new BlockInfo(pos, in.state));
        }

        return list;
    }

    private static BlockPos readPos(ListNBT list) {
        int x = list.getInt(0);
        int y = list.getInt(1);
        int z = list.getInt(2);
        return new BlockPos(x, y, z);
    }
}
