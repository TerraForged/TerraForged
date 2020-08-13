package com.terraforged.mod.server.command.search;

import com.terraforged.core.cell.Cell;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;

public class BiomeSearchTask extends ChunkGeneratorSearch {

    private final Biome biome;
    private final TerraBiomeProvider biomeProvider;

    private final Cell cell = new Cell();

    public BiomeSearchTask(BlockPos center, Biome biome, ChunkGenerator generator, TerraBiomeProvider biomeProvider) {
        super(center, generator);
        this.biomeProvider = biomeProvider;
        this.biome = biome;
    }

    @Override
    public int getSpacing() {
        return 10;
    }

    @Override
    public boolean test(BlockPos pos) {
        biomeProvider.getWorldLookup().applyCell(cell, pos.getX(), pos.getZ());
        if (biomeProvider.getBiome(cell, pos.getX(), pos.getZ()) == biome) {
            return biomeProvider.getBiome(pos.getX(), pos.getZ()) == biome;
        }
        return false;
    }
}
