package com.terraforged.mod.feature.feature;

import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
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
        setRegistryName("terraforged", "disk");
    }

    @Override
    public boolean func_241855_a(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, SphereReplaceConfig config) {
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
                        for(int y = pos.getY() - ySize; y <= pos.getY() + ySize && y + 1 < generator.func_230356_f_(); ++y) {
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
