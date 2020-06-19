package com.terraforged.command.search;

import com.terraforged.biome.provider.TerraBiomeProvider;
import com.terraforged.core.cell.Cell;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;

public class BiomeSearchTask extends ChunkGeneratorSearch {

    private final Biome biome;
    private final TerraBiomeProvider biomeProvider;

    private final Cell cell = new Cell();

    public BiomeSearchTask(BlockPos center, Biome biome, ChunkGenerator<?> generator, TerraBiomeProvider biomeProvider) {
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
        return biomeProvider.getBiome(cell, pos.getX(), pos.getZ()) == biome;
    }
}
