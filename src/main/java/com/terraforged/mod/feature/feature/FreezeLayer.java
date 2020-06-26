package com.terraforged.mod.feature.feature;

import com.terraforged.fm.template.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

public class FreezeLayer extends Feature<NoFeatureConfig> {

    public static final FreezeLayer INSTANCE = new FreezeLayer();

    public FreezeLayer() {
        super(NoFeatureConfig::deserialize);
        setRegistryName("terraforged", "freeze_top_layer");
    }

    @Override
    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        BlockPos.Mutable pos1 = new BlockPos.Mutable();
        BlockPos.Mutable pos2 = new BlockPos.Mutable();

        for(int dx = 0; dx < 16; ++dx) {
            for(int dz = 0; dz < 16; ++dz) {
                int x = pos.getX() + dx;
                int z = pos.getZ() + dz;
                int y1 = world.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z);
                int y2 = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
                pos1.setPos(x, y1, z);
                pos2.setPos(pos1).move(Direction.DOWN, 1);

                Biome biome = world.getBiome(pos1);
                boolean freezesHere = freeze(world, biome, pos1, pos2, false, false);

                if (y1 != y2) {
                    pos1.setPos(x, y2, z);
                    pos2.setPos(pos1).move(Direction.DOWN, 1);
                    freeze(world, biome, pos1, pos2, freezesHere, true);
                }
            }
        }

        return true;
    }

    private boolean freeze(IWorld world, Biome biome, BlockPos.Mutable top, BlockPos below, boolean force, boolean ground) {
        boolean hasFrozen = false;
        if (biome.doesWaterFreeze(world, below, false)) {
            world.setBlockState(below, Blocks.ICE.getDefaultState(), 2);
            hasFrozen = true;
        }

        if (force || biome.doesSnowGenerate(world, top)) {
            hasFrozen = true;
            BlockState stateUnder = world.getBlockState(below);

            if (stateUnder.getBlock() == Blocks.AIR) {
                return false;
            }

            if (ground) {
                if (BlockTags.LOGS.contains(stateUnder.getBlock())) {
                    return false;
                }

                top.move(Direction.UP, 1);
                BlockState above = world.getBlockState(top);
                if (BlockTags.LOGS.contains(above.getBlock()) || BlockTags.LEAVES.contains(above.getBlock())) {
                    return false;
                }

                top.move(Direction.DOWN, 1);
                if (setSnow(world, top, below, stateUnder)) {
                    if (above.getBlock() != Blocks.AIR) {
                        world.setBlockState(top, Blocks.AIR.getDefaultState(), 2);
                    }
                }
            } else {
                setSnow(world, top, below, stateUnder);
            }
        }
        return hasFrozen;
    }

    private boolean setSnow(IWorld world, BlockPos pos1, BlockPos pos2, BlockState below) {
        if (BlockUtils.isSolid(world, pos1)) {
            return false;
        }

        world.setBlockState(pos1, Blocks.SNOW.getDefaultState(), 2);

        if (below.has(SnowyDirtBlock.SNOWY)) {
            world.setBlockState(pos2, below.with(SnowyDirtBlock.SNOWY, true), 2);
        }
        return true;
    }
}
