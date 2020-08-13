package com.terraforged.mod.server.command.search;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.WorldGenerator;
import com.terraforged.world.terrain.Terrain;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;

public class TerrainSearchTask extends ChunkGeneratorSearch {

    private final Terrain type;
    private final WorldGenerator worldGenerator;
    private final Cell cell = new Cell();

    public TerrainSearchTask(BlockPos center, Terrain type, ChunkGenerator chunkGenerator, WorldGenerator worldGenerator) {
        super(center, 256, chunkGenerator);
        this.type = type;
        this.worldGenerator = worldGenerator;
    }

    @Override
    public int getSpacing() {
        return 20;
    }

    @Override
    public boolean test(BlockPos pos) {
        worldGenerator.getHeightmap().apply(cell, pos.getX(), pos.getZ());
        return cell.terrain == type;
    }
}
