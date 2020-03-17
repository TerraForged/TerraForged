package com.terraforged.mod.feature.feature;

import me.dags.noise.Module;
import me.dags.noise.Source;
import net.minecraft.block.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SphereReplaceConfig;

import java.util.Random;

public class DiskFeature extends Feature<SphereReplaceConfig> {

    public static final DiskFeature INSTANCE = new DiskFeature();

    private final Module domain = Source.simplex(1, 6, 3);

    private DiskFeature() {
        super(SphereReplaceConfig::deserialize);
        setRegistryName("terraforged", "disk");
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, SphereReplaceConfig config) {
        if (!worldIn.getFluidState(pos).isTagged(FluidTags.WATER)) {
            return false;
        } else {
            int i = 0;
            float radius2 = (config.radius * config.radius)  * 0.65F;
            BlockPos.Mutable blockPos = new BlockPos.Mutable();

            for(int x = pos.getX() - config.radius; x <= pos.getX() + config.radius; ++x) {
                for(int z = pos.getZ() - config.radius; z <= pos.getZ() + config.radius; ++z) {
                    int dx = x - pos.getX();
                    int dz = z - pos.getZ();
                    float rad2 = domain.getValue(x, z) * radius2;
                    if (dx * dx + dz * dz <= rad2) {
                        for(int y = pos.getY() - config.ySize; y <= pos.getY() + config.ySize && y + 1 < generator.getSeaLevel(); ++y) {
                            blockPos.setPos(x, y, z);
                            BlockState current = worldIn.getBlockState(blockPos);

                            for(BlockState target : config.targets) {
                                if (target.getBlock() == current.getBlock()) {
                                    worldIn.setBlockState(blockPos, config.state, 2);
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
