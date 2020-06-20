package com.terraforged.feature.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.terraforged.fm.template.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.Random;

public class BushFeature extends Feature<BushFeature.Config> {

    public static final BushFeature INSTANCE = new BushFeature();

    private static final Vec3i[] logs = {
            new Vec3i(+1, 0, +1),
            new Vec3i(+1, 0, -1),
            new Vec3i(-1, 0, -1),
            new Vec3i(-1, 0, +1),

            new Vec3i(+2, 0, +1),
            new Vec3i(+2, 0, -1),
            new Vec3i(-2, 0, +1),
            new Vec3i(-2, 0, -1),

            new Vec3i(+1, 0, +2),
            new Vec3i(+1, 0, -2),
            new Vec3i(-1, 0, +2),
            new Vec3i(-1, 0, -2),
    };

    private static final Vec3i[] leaves = {
            new Vec3i(0, 0, 1),
            new Vec3i(0, 0, -1),
            new Vec3i(1, 0, 0),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, 1, 0),
    };

    public BushFeature() {
        super(BushFeature::deserialize);
        setRegistryName("terraforged", "bush");
    }

    @Override
    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, Config config) {
        try (BlockPos.PooledMutable log = BlockPos.PooledMutable.retain(); BlockPos.PooledMutable leaf = BlockPos.PooledMutable.retain()) {
            place(world, log.setPos(pos), leaf, rand, config);
            for (float chance = rand.nextFloat(); chance < config.size_chance; chance += rand.nextFloat()) {
                add(log, logs[rand.nextInt(logs.length)]);

                if (!place(world, log, leaf, rand, config)) {
                    break;
                }
            }
        }
        return true;
    }

    private boolean place(IWorld world, BlockPos.Mutable center, BlockPos.Mutable pos, Random random, Config config) {
        center.setY(world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, center.getX(), center.getZ()));

        // don't replace solid blocks
        if (BlockUtils.isSolid(world, center)) {
            return false;
        }

        // only place on solid blocks
        center.move(Direction.DOWN, 1);
        if (!BlockUtils.isSolid(world, center)) {
            return false;
        }

        center.move(Direction.UP, 1);
        world.setBlockState(center, config.trunk, 2);

        for (Vec3i neighbour : leaves) {
            // randomly skip NESW neighbours
            if (neighbour.getY() == 0 && random.nextFloat() < config.airChance) {
                continue;
            }

            pos.setPos(center);
            add(pos, neighbour);

            if (!BlockUtils.isSolid(world, pos)) {
                world.setBlockState(pos, config.leaves, 2);

                // randomly place extra leaves below if non-solid
                if (neighbour.getY() == 0 && random.nextFloat() < config.leafChance) {
                    pos.move(Direction.DOWN, 1);
                    if (!BlockUtils.isSolid(world, pos)) {
                        world.setBlockState(pos, config.leaves, 2);
                    }
                    pos.move(Direction.UP, 1);
                }

                // randomly place extra leaves above
                if (neighbour.getY() == 0 && random.nextFloat() < config.leafChance) {
                    pos.move(Direction.UP, 1);
                    if (!BlockUtils.isSolid(world, pos)) {
                        world.setBlockState(pos, config.leaves, 2);
                    }
                }
            }
        }

        return true;
    }

    private static void add(BlockPos.Mutable pos, Vec3i add) {
        pos.setPos(pos.getX() + add.getX(), pos.getY() + add.getY(), pos.getZ() + add.getZ());
    }

    public static Config deserialize(Dynamic<?> data) {
        BlockState logs = BlockState.deserialize(data.get("trunk").get().get());
        BlockState leaves = BlockState.deserialize(data.get("leaves").get().get());
        float airChance = data.get("air_chance").asFloat(0.075F);
        float leafChance = data.get("leaf_chance").asFloat(0.075F);
        float sizeChance = data.get("size_chance").asFloat(0.75F);
        return new Config(logs, leaves, airChance, leafChance, sizeChance);
    }

    public static class Config implements IFeatureConfig {

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

        @Override
        public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
            return new Dynamic<>(
                    ops,
                    ops.createMap(ImmutableMap.of(
                            ops.createString("trunk"), BlockState.serialize(ops, trunk).getValue(),
                            ops.createString("leaves"), BlockState.serialize(ops, leaves).getValue(),
                            ops.createString("air_chance"), ops.createFloat(airChance),
                            ops.createString("leaf_chance"), ops.createFloat(airChance),
                            ops.createString("size_chance"), ops.createFloat(size_chance)
                    ))
            );
        }
    }
}
