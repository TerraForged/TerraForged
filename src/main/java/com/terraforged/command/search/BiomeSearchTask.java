package com.terraforged.command.search;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;

public class BiomeSearchTask extends Search {

    private final Biome biome;
    private final IWorldReader reader;

    public BiomeSearchTask(BlockPos center, IWorldReader reader, Biome biome) {
        super(center, 128);
        this.reader = reader;
        this.biome = biome;
    }

    @Override
    public int getSpacing() {
        return 10;
    }

    @Override
    public boolean test(BlockPos pos) {
        return reader.getNoiseBiomeRaw(pos.getX() >> 2, pos.getY(), pos.getZ() >> 2) == biome;
    }

    @Override
    public BlockPos success(BlockPos.Mutable pos) {
        pos.setY(reader.getHeight(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()));
        return super.success(pos);
    }
}
