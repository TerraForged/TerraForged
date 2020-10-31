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

package com.terraforged.fm.template;

import com.terraforged.core.concurrent.pool.ThreadLocalPool;
import com.terraforged.fm.template.buffer.BufferIterator;
import com.terraforged.fm.template.buffer.PasteBuffer;
import com.terraforged.fm.template.buffer.TemplateBuffer;
import com.terraforged.fm.util.BlockReader;
import com.terraforged.fm.util.ObjectPool;
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
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.common.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Template {

    private static final int pasteFlag = 3 | 16;
    private static final Direction[] directions = Direction.values();
    private static final ThreadLocal<PasteBuffer> PASTE_BUFFER = ThreadLocal.withInitial(PasteBuffer::new);
    private static final ThreadLocal<TemplateBuffer> TEMPLATE_BUFFER = ThreadLocal.withInitial(TemplateBuffer::new);

    private final BlockPos min;
    private final BlockPos max;
    private final List<BlockInfo> blocks;

    public Template(List<BlockInfo> blocks) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int i = 0; i < blocks.size(); i++) {
            BlockInfo block = blocks.get(i);
            minX = Math.min(minX, block.pos.getX());
            minY = Math.min(minY, block.pos.getY());
            minZ = Math.min(minZ, block.pos.getZ());
            maxX = Math.max(maxX, block.pos.getX());
            maxY = Math.max(maxY, block.pos.getY());
            maxZ = Math.max(maxZ, block.pos.getZ());
        }
        this.blocks = blocks;
        this.min = new BlockPos(minX, minY, minZ);
        this.max = new BlockPos(maxX, maxY, maxZ);
    }

    public boolean paste(IWorld world, BlockPos origin, Mirror mirror, Rotation rotation, PasteConfig config) {
        if (config.checkBounds) {
            IChunk chunk = world.getChunk(origin);
            if (StructureUtils.hasOvergroundStructure(chunk)) {
                return pasteWithBoundsCheck(world, origin, mirror, rotation, config);
            }
        }
        return pasteNormal(world, origin, mirror, rotation, config);
    }

    public boolean pasteNormal(IWorld world, BlockPos origin, Mirror mirror, Rotation rotation, PasteConfig config) {
        boolean placed = false;
        BlockReader reader = new BlockReader();
        PasteBuffer buffer = PASTE_BUFFER.get().configure(config);

        for (int i = 0; i < blocks.size(); i++) {
            BlockInfo block = blocks.get(i);
            BlockState state = block.state.mirror(mirror).rotate(rotation);
            if (!config.pasteAir && state.getBlock() == Blocks.AIR) {
                continue;
            }

            BlockPos pos = transform(block.pos, mirror, rotation).add(origin);
            if (!config.replaceSolid && BlockUtils.isSolid(world, pos)) {
                continue;
            }

            if (block.pos.getY() <= 0 && block.state.isNormalCube(reader.setState(block.state), BlockPos.ZERO)) {
                placeBase(world, pos, block.state, config.baseDepth);
            }

            world.setBlockState(pos, state, 2);
            buffer.record(pos);

            placed = true;
        }

        return placed;
    }

    public boolean pasteWithBoundsCheck(IWorld world, BlockPos origin, Mirror mirror, Rotation rotation, PasteConfig config) {
        BlockPos min = transform(this.min, mirror, rotation).add(origin);
        BlockPos max = transform(this.max, mirror, rotation).add(origin);
        TemplateBuffer buffer = TEMPLATE_BUFFER.get().init(world, origin, min, max).configure(config);

        List<BlockInfo> blocks = this.blocks;
        for (int i = 0; i < blocks.size(); i++) {
            BlockInfo block = blocks.get(i);
            BlockState state = block.state.mirror(mirror).rotate(rotation);
            BlockPos pos = transform(block.pos, mirror, rotation).add(origin);
            int cx = pos.getX() >> 4;
            int cz = pos.getZ() >> 4;
            if (!world.chunkExists(cx, cz)) {
                return false;
            }
            buffer.record(pos, state, config);
        }

        boolean placed = false;
        BlockReader reader = new BlockReader();
        blocks = buffer.getBlocks();

        for (int i = 0; i < blocks.size(); i++) {
            BlockInfo block = blocks.get(i);
            if (block.pos.getY() <= origin.getY() && block.state.isNormalCube(reader.setState(block.state), BlockPos.ZERO)) {
                placeBase(world, block.pos, block.state, config.baseDepth);
                world.setBlockState(block.pos, block.state, 2);
                placed = true;
            } else if (buffer.test(block.pos)) {
                placed = true;
                world.setBlockState(block.pos, block.state, 2);
                buffer.record(block.pos);
            }
        }

        buffer.flush();
        return placed;
    }

    private static void updatePostPlacement(IWorld world, BufferIterator iterator) {
        if (iterator.size() > 0) {
            while (iterator.next()) {
                BlockPos pos = iterator.getPos();
                for (Direction direction : directions) {
                    updatePostPlacement(world, pos, direction);
                }
            }
        }
    }

    private static void updatePostPlacement(IWorld world, BlockPos pos1, Direction direction) {
        BlockPos pos2 = pos1.offset(direction);
        BlockState state1 = world.getBlockState(pos1);
        BlockState state2 = world.getBlockState(pos2);

        // update state at pos1 - the input position
        BlockState result1 = state1.updatePostPlacement(direction, state2, world, pos1, pos2);
        if (result1 != state1) {
            world.setBlockState(pos1, result1, pasteFlag);
        }

        // update state at pos2 - the neighbour
        BlockState result2 = state2.updatePostPlacement(direction.getOpposite(), result1, world, pos2, pos1);
        if (result2 != state2) {
            world.setBlockState(pos2, result2, pasteFlag);
        }
    }

    private void placeBase(IWorld world, BlockPos pos, BlockState state, int depth) {
        for (int dy = 0; dy < depth; dy++) {
            pos = pos.down();
            if (world.getBlockState(pos).isSolid()) {
                return;
            }
            world.setBlockState(pos, state, 2);
        }
    }

    public static BlockPos transform(BlockPos pos, Mirror mirror, Rotation rotation) {
        return net.minecraft.world.gen.feature.template.Template.getTransformedPos(pos, mirror, rotation, BlockPos.ZERO);
    }

    public static class BlockInfo {

        private final BlockPos pos;
        private final BlockState state;

        public BlockInfo(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        public BlockPos getPos() {
            return pos;
        }

        public BlockState getState() {
            return state;
        }

        @Override
        public String toString() {
            return state.toString();
        }
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
            if (!block.state.isSolid()) {
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
