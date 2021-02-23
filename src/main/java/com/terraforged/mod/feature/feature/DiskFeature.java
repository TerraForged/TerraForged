/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

import com.terraforged.mod.TerraForgedMod;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.block.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SphereReplaceConfig;

import java.util.Random;

public class DiskFeature extends Feature<SphereReplaceConfig> {

    public static final DiskFeature INSTANCE = new DiskFeature();

    private final Module domain = Source.simplex(1, 6, 3);

    private DiskFeature() {
        super(SphereReplaceConfig.field_236516_a_);
        setRegistryName(TerraForgedMod.MODID, "disk");
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, SphereReplaceConfig config) {
        if (!world.getFluidState(pos).isTagged(FluidTags.WATER)) {
            return false;
        } else {
            int cRadius = 6;
            int ySize = 5;

            int i = 0;
            int radius = 4 + rand.nextInt(cRadius);
            float radius2 = (radius * radius)  * 0.65F;
            BlockPos.Mutable blockPos = new BlockPos.Mutable();

            for(int x = pos.getX() - radius; x <= pos.getX() + radius; ++x) {
                for(int z = pos.getZ() - radius; z <= pos.getZ() + radius; ++z) {
                    int dx = x - pos.getX();
                    int dz = z - pos.getZ();
                    float rad2 = domain.getValue(x, z) * radius2;
                    if (dx * dx + dz * dz <= rad2) {
                        for(int y = pos.getY() - ySize; y <= pos.getY() + ySize && y + 1 < generator.getMaxBuildHeight(); ++y) {
                            blockPos.setPos(x, y, z);
                            BlockState current = world.getBlockState(blockPos);

                            for(BlockState target : config.targets) {
                                if (target.getBlock() == current.getBlock()) {
                                    world.setBlockState(blockPos, config.state, 2);
                                    ++i;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return i > 0;
        }
    }
}
