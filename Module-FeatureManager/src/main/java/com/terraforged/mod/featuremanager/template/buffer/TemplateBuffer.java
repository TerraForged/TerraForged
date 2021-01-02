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

package com.terraforged.mod.featuremanager.template.buffer;

import com.terraforged.mod.featuremanager.template.feature.Placement;
import com.terraforged.mod.featuremanager.template.paste.PasteConfig;
import com.terraforged.mod.featuremanager.template.template.BlockInfo;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;

public class TemplateBuffer extends PasteBuffer {

    private IWorld world;
    private BlockPos origin;
    private final BufferBitSet placementMask = new BufferBitSet();

    public TemplateBuffer() {
        setRecording(true);
    }

    public TemplateBuffer init(IWorld world, BlockPos origin, Vector3i p1, Vector3i p2) {
        super.clear();
        this.placementMask.clear();
        this.world = world;
        this.origin = origin;
        this.placementMask.set(p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
        return this;
    }

    public void record(int i, BlockInfo block, BlockPos pastePos, Placement placement, PasteConfig config) {
        if (!config.replaceSolid && !placement.canReplaceAt(world, pastePos)) {
            placementMask.set(block.pos.getX(), block.pos.getY(), block.pos.getZ());
            return;
        }

        if (!config.pasteAir && block.state.getBlock() == Blocks.AIR) {
            return;
        }

        record(i);
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
        int x = start.getX();
        int z = start.getZ();
        float px = x;
        float pz = z;
        int count = 0;
        while (x != origin.getX() && z != origin.getZ() && count < 10) {
            if (placementMask.test(x, start.getY(), z)) {
                return false;
            }
            px -= dx;
            pz -= dz;
            x = NoiseUtil.floor(px);
            z = NoiseUtil.floor(pz);
            count++;
        }
        return true;
    }
}
