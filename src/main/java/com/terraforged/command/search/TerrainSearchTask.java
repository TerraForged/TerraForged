package com.terraforged.command.search;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.WorldGenerator;
import com.terraforged.world.terrain.Terrain;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.Heightmap;

public class TerrainSearchTask extends Search {

    private final Terrain type;
    private final IWorldReader reader;
    private final WorldGenerator generator;
    private final Cell cell = new Cell();

    public TerrainSearchTask(BlockPos center, IWorldReader reader, WorldGenerator generator, Terrain type) {
        super(center, 256);
        this.type = type;
        this.generator = generator;
        this.reader = reader;
    }

    @Override
    public int getSpacing() {
        return 20;
    }

    @Override
    public boolean test(BlockPos pos) {
        generator.getHeightmap().apply(cell, pos.getX(), pos.getZ());
        return cell.terrain == type;
    }

    @Override
    public BlockPos success(BlockPos.Mutable pos) {
        pos.setY(reader.getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ()));
        return super.success(pos);
    }
}
