package com.terraforged.feature.feature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyDirtBlock;
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
                boolean freezesHere = freeze(world, biome, pos1, pos2, false);

                if (y1 != y2) {
                    pos1.setPos(x, y2, z);
                    pos2.setPos(pos1).move(Direction.DOWN, 1);
                    freeze(world, biome, pos1, pos2, freezesHere);
                }
            }
        }

        return true;
    }

    private boolean freeze(IWorld world, Biome biome, BlockPos top, BlockPos below, boolean force) {
        boolean hasFrozen = false;
        if (biome.doesWaterFreeze(world, below, false)) {
            world.setBlockState(below, Blocks.ICE.getDefaultState(), 2);
            hasFrozen = true;
        }

        if (force || biome.doesSnowGenerate(world, top)) {
            hasFrozen = true;
            world.setBlockState(top, Blocks.SNOW.getDefaultState(), 2);
            BlockState blockstate = world.getBlockState(below);
            if (blockstate.has(SnowyDirtBlock.SNOWY)) {
                world.setBlockState(below, blockstate.with(SnowyDirtBlock.SNOWY, true), 2);
            }
        }
        return hasFrozen;
    }
}
