package com.terraforged.biome.spawn;

import com.terraforged.biome.provider.BiomeProvider;
import com.terraforged.command.search.Search;
import com.terraforged.core.cell.Cell;
import com.terraforged.Log;
import net.minecraft.util.math.BlockPos;

public class SpawnSearch extends Search {

    private final BiomeProvider biomeProvider;
    private final Cell cell = new Cell();

    public SpawnSearch(BlockPos center, BiomeProvider biomeProvider) {
        super(center, 0, 2048);
        this.biomeProvider = biomeProvider;
    }

    @Override
    public int getSpacing() {
        return 64;
    }

    @Override
    public boolean test(BlockPos pos) {
        biomeProvider.getWorldLookup().applyCell(cell, pos.getX(), pos.getZ());
        return biomeProvider.canSpawnAt(cell);
    }

    @Override
    public BlockPos success(BlockPos.Mutable pos) {
        pos.setY(biomeProvider.getContext().levels.scale(cell.value));
        Log.info("Found valid spawn position: {}", pos);
        return super.success(pos);
    }

    @Override
    public BlockPos fail(BlockPos pos) {
        Log.info("Unable to find valid spawn position, defaulting x=0, z=0");
        return new BlockPos(0, biomeProvider.getContext().levels.groundLevel, 0);
    }
}
