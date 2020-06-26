package com.terraforged.mod.server.command.search;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;

public abstract class ChunkGeneratorSearch extends Search {

    private final ChunkGenerator<?> chunkGenerator;

    public ChunkGeneratorSearch(BlockPos center, ChunkGenerator<?> chunkGenerator) {
        super(center);
        this.chunkGenerator = chunkGenerator;
    }

    public ChunkGeneratorSearch(BlockPos center, int minRadius, ChunkGenerator<?> chunkGenerator) {
        this(center, minRadius, MAX_RADIUS, chunkGenerator);
    }

    public ChunkGeneratorSearch(BlockPos center, int minRadius, int maxRadius, ChunkGenerator<?> chunkGenerator) {
        super(center, minRadius, maxRadius);
        this.chunkGenerator = chunkGenerator;
    }

    @Override
    public BlockPos success(BlockPos.Mutable pos) {
        pos.setY(chunkGenerator.func_222529_a(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG));
        return pos;
    }
}
