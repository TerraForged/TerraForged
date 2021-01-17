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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.featuremanager.template.BlockUtils;
import com.terraforged.mod.featuremanager.util.codec.CodecHelper;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.Random;

public class BushFeature extends Feature<BushFeature.Config> {

    public static final BushFeature INSTANCE = new BushFeature();

    private static final Vector3i[] logs = {
            new Vector3i(+1, 0, +1),
            new Vector3i(+1, 0, -1),
            new Vector3i(-1, 0, -1),
            new Vector3i(-1, 0, +1),

            new Vector3i(+2, 0, +1),
            new Vector3i(+2, 0, -1),
            new Vector3i(-2, 0, +1),
            new Vector3i(-2, 0, -1),

            new Vector3i(+1, 0, +2),
            new Vector3i(+1, 0, -2),
            new Vector3i(-1, 0, +2),
            new Vector3i(-1, 0, -2),
    };

    private static final Vector3i[] leaves = {
            new Vector3i(0, 0, 1),
            new Vector3i(0, 0, -1),
            new Vector3i(1, 0, 0),
            new Vector3i(-1, 0, 0),
            new Vector3i(0, 1, 0),
    };

    public BushFeature() {
        super(Config.CODEC);
        setRegistryName(TerraForgedMod.MODID, "bush");
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, Config config) {
        BlockPos.Mutable log = new BlockPos.Mutable();
        BlockPos.Mutable leaf = new BlockPos.Mutable();
        place(world, log.setPos(pos), leaf, rand, config);
        for (float chance = rand.nextFloat(); chance < config.size_chance; chance += rand.nextFloat()) {
            addToMutable(log, logs[rand.nextInt(logs.length)]);

            if (!place(world, log, leaf, rand, config)) {
                break;
            }
        }
        return false;
    }

    private boolean place(IWorld world, BlockPos.Mutable center, BlockPos.Mutable pos, Random random, Config config) {
        center.setY(world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, center.getX(), center.getZ()));

        // don't replace solid blocks
        if (!BlockUtils.canTreeReplace(world, center)) {
            return false;
        }

        // only place on solid non-ice blocks
        center.move(Direction.DOWN, 1);
        if (!BlockUtils.isSolidNoIce(world, center)) {
            return false;
        }

        center.move(Direction.UP, 1);
        world.setBlockState(center, config.trunk, 2);

        BlockState leaves = config.leavesWithDistance(1);
        BlockState leavesExtra = config.leavesWithDistance(2);
        for (Vector3i neighbour : BushFeature.leaves) {
            // randomly skip NESW neighbours
            if (neighbour.getY() == 0 && random.nextFloat() < config.airChance) {
                continue;
            }

            pos.setPos(center);
            addToMutable(pos, neighbour);

            if (BlockUtils.canTreeReplace(world, pos)) {
                world.setBlockState(pos, leaves, 2);

                // randomly place extra leaves below if non-solid
                if (neighbour.getY() == 0 && random.nextFloat() < config.leafChance) {
                    pos.move(Direction.DOWN, 1);
                    if (BlockUtils.canTreeReplace(world, pos)) {
                        world.setBlockState(pos, leavesExtra, 2);
                    }
                    pos.move(Direction.UP, 1);
                }

                // randomly place extra leaves above
                if (neighbour.getY() == 0 && random.nextFloat() < config.leafChance) {
                    pos.move(Direction.UP, 1);
                    if (BlockUtils.canTreeReplace(world, pos)) {
                        world.setBlockState(pos, leavesExtra, 2);
                    }
                }
            }
        }

        return true;
    }

    private static void addToMutable(BlockPos.Mutable pos, Vector3i add) {
        pos.setPos(pos.getX() + add.getX(), pos.getY() + add.getY(), pos.getZ() + add.getZ());
    }

    public static <T> Dynamic<T> serialize(Config config, DynamicOps<T> ops) {
        return new Dynamic<>(
                ops,
                ops.createMap(ImmutableMap.of(
                        ops.createString("trunk"), CodecHelper.setState(config.trunk, ops),
                        ops.createString("leaves"), CodecHelper.setState(config.leaves, ops),
                        ops.createString("air_chance"), ops.createFloat(config.airChance),
                        ops.createString("leaf_chance"), ops.createFloat(config.airChance),
                        ops.createString("size_chance"), ops.createFloat(config.size_chance)
                ))
        );
    }

    public static <T> Config deserialize(Dynamic<T> data) {
        BlockState logs = CodecHelper.getState(data.get("trunk"));
        BlockState leaves = CodecHelper.getState(data.get("leaves"));
        float airChance = data.get("air_chance").asFloat(0.075F);
        float leafChance = data.get("leaf_chance").asFloat(0.075F);
        float sizeChance = data.get("size_chance").asFloat(0.75F);
        return new Config(logs, leaves, airChance, leafChance, sizeChance);
    }

    public static class Config implements IFeatureConfig {

        public static final Codec<Config> CODEC = Codecs.create(
                BushFeature::serialize,
                BushFeature::deserialize
        );

        private final BlockState trunk;
        private final BlockState leaves;
        private final float airChance;
        private final float leafChance;
        private final float size_chance;

        public Config(BlockState trunk, BlockState leaves, float airChance, float leafChance, float size_chance) {
            this.trunk = trunk;
            this.leaves = leaves;
            this.airChance = airChance;
            this.leafChance = leafChance;
            this.size_chance = size_chance;
        }

        public BlockState leavesWithDistance(int distance) {
            if (leaves.hasProperty(LeavesBlock.DISTANCE)) {
                return leaves.with(LeavesBlock.DISTANCE, distance);
            }
            return leaves;
        }
    }
}
