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

import com.terraforged.feature.template.feature.TemplateFeature;
import com.terraforged.feature.util.WorldDelegate;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.server.ServerChunkProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TreeBuffer extends WorldDelegate {

    private int topY = 0;
    private final BlockPos pos;
    private final MutableVec3i baseMin = new MutableVec3i(0);
    private final MutableVec3i baseMax = new MutableVec3i(0);
    private final List<TemplateFeature.BlockInfo> changes = new LinkedList<>();

    public TreeBuffer(IWorld delegate, BlockPos pos) {
        super(delegate);
        this.pos = pos;
    }

    public int getTopY() {
        return topY;
    }

    public BlockPos getBaseMin() {
        return new BlockPos(-baseMin.x, baseMin.y, -baseMin.z);
    }

    public BlockPos getBaseMax() {
        return new BlockPos(baseMax.x, baseMax.y, baseMax.z);
    }

    public Iterable<TemplateFeature.BlockInfo> getChanges() {
        return changes;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int i) {
        if (state.isSolid()) {
            recordPos(pos);
        }
        changes.add(new TemplateFeature.BlockInfo(pos, state));
        return true;
    }

    public void placeFeature(Feature<NoFeatureConfig> feature, BlockPos pos, Random random) {
        placeFeature(feature, pos, random, NoFeatureConfig.NO_FEATURE_CONFIG);
    }

    public <T extends IFeatureConfig> void placeFeature(Feature<T> feature, BlockPos pos, Random random, T config) {
        if (getChunkProvider() instanceof ServerChunkProvider) {
            ServerChunkProvider chunkProvider = (ServerChunkProvider) getChunkProvider();
            feature.place(this, chunkProvider.getChunkGenerator(), random, pos, config);
        }
    }

    private void recordPos(BlockPos pos) {
        if (pos.getY() > topY) {
            topY = pos.getY();
            baseMax.max(pos.subtract(this.pos));
        } else if (pos.getY() == this.pos.getY()) {
            baseMin.min(pos.subtract(this.pos));
        }
    }

    private static class MutableVec3i {

        private int x, y, z;

        private MutableVec3i(int start) {
            x = start;
            y = start;
            z = start;
        }

        private void min(Vec3i vec) {
            x = Math.min(x, vec.getX());
            y = Math.min(y, vec.getY());
            z = Math.min(z, vec.getZ());
        }

        private void max(Vec3i vec) {
            x = Math.max(x, vec.getX());
            y = Math.max(y, vec.getY());
            z = Math.max(z, vec.getZ());
        }
    }
}
