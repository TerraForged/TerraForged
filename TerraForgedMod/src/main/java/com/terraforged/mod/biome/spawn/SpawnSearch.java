package com.terraforged.mod.biome.spawn;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.BiomeProvider;
import com.terraforged.mod.command.search.Search;
import net.minecraft.util.math.BlockPos;

public class SpawnSearch extends Search {

    private final BiomeProvider biomeProvider;
    private final Cell<Terrain> cell = new Cell<>();

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
        biomeProvider.lookupPos(pos.getX(), pos.getZ(), cell);
        return biomeProvider.canSpawnAt(cell);
    }

    @Override
    protected BlockPos success(BlockPos.Mutable pos) {
        pos.setY(biomeProvider.getContext().levels.scale(cell.value));
        Log.info("Found valid spawn position: {}", pos);
        return super.success(pos);
    }

    @Override
    protected BlockPos fail(BlockPos pos) {
        Log.info("Unable to find valid spawn position, defaulting x=0, z=0");
        return new BlockPos(0, biomeProvider.getContext().levels.groundLevel, 0);
    }
}
