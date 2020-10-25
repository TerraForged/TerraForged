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

package com.terraforged.fm.template.buffer;

import com.terraforged.fm.template.BlockUtils;
import com.terraforged.fm.template.PasteConfig;
import com.terraforged.fm.template.Template;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;

import java.util.*;

public class TemplateBuffer extends PasteBuffer {

    private IWorld world;
    private BlockPos origin;

    private final BufferBitSet bitSet = new BufferBitSet();
    private final List<Template.BlockInfo> buffer = new ArrayList<>(128);

    public TemplateBuffer init(IWorld world, BlockPos origin, Vector3i min, Vector3i max) {
        int minX = min.getX();
        int minY = min.getY();
        int minZ = min.getZ();
        int sizeX = max.getX() - min.getX();
        int sizeY = max.getY() - min.getY();
        int sizeZ = max.getZ() - min.getZ();
        this.world = world;
        this.origin = origin;
        this.bitSet.assign(minX, minY, minZ, sizeX, sizeY, sizeZ);
        return this;
    }

    @Override
    public TemplateBuffer configure(PasteConfig config) {
        super.configure(config);
        return this;
    }

    public void flush() {
        this.buffer.clear();
        this.bitSet.clear();
    }

    public List<Template.BlockInfo> getBlocks() {
        return buffer;
    }

    public void record(BlockPos pos, BlockState state, PasteConfig config) {
        if (!config.replaceSolid && BlockUtils.isSolid(world, pos)) {
            bitSet.set(pos.getX(), pos.getY(), pos.getZ());
            return;
        }

        if (!config.pasteAir && state.getBlock() == Blocks.AIR) {
            return;
        }

        buffer.add(new Template.BlockInfo(pos, state));
    }

    public boolean test(BlockPos pos) {
        int dx = pos.getX() - origin.getX();
        int dz = pos.getZ() - origin.getZ();
        if (dx == dz) {
            return test(pos, 1, 1);
        }
        if (Math.abs(dx) > Math.abs(dz)) {
            return test(pos, 1F, dz / (float) dx);
        } else {
            return test(pos, dx / (float) dz, 1F);
        }
    }

    private boolean test(BlockPos start, float dx, float dz) {
        float x = start.getX();
        float z = start.getZ();
        int count = 0;
        while (x != origin.getX() && z != origin.getZ() && count < 10) {
            if (bitSet.test(origin.getX(), start.getY(), origin.getZ())) {
                return false;
            }
            x -= dx;
            z -= dz;
            count++;
        }
        return true;
    }
}
