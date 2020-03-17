package com.terraforged.mod.feature.placement;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.placement.HeightWithChanceConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.Random;
import java.util.stream.Stream;

public class BetterAtSurfaceWithChanceMultiple extends Placement<HeightWithChanceConfig> {

    public BetterAtSurfaceWithChanceMultiple() {
        super(HeightWithChanceConfig::deserialize);
    }

    @Override
    public Stream<BlockPos> getPositions(IWorld world, ChunkGenerator<?> generator, Random random, HeightWithChanceConfig config, BlockPos pos) {
        return PosStream.of(config.count, next -> {
            if (random.nextFloat() < config.chance) {
                int x = random.nextInt(16) + pos.getX();
                int z = random.nextInt(16) + pos.getZ();
                int y = world.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z);
                next.setPos(x, y, z);
                return true;
            }
            return false;
        });
    }
}
