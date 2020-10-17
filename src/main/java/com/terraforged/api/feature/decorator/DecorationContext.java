package com.terraforged.api.feature.decorator;

import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.fix.RegionDelegate;
import com.terraforged.mod.chunk.util.TerraContainer;
import com.terraforged.world.heightmap.Levels;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;

public class DecorationContext {

    private final IChunk chunk;
    private final Levels levels;
    private final WorldGenRegion region;
    private final TerraContainer biomes;
    private final TerraChunkGenerator generator;

    public DecorationContext(WorldGenRegion region, IChunk chunk, TerraContainer biomes, TerraChunkGenerator generator) {
        this.chunk = chunk;
        this.region = region;
        this.biomes = biomes;
        this.generator = generator;
        this.levels = generator.getContext().levels;
    }

    public IChunk getChunk() {
        return chunk;
    }

    public WorldGenRegion getRegion() {
        return region;
    }

    public TerraContainer getBiomes() {
        return biomes;
    }

    public Biome getBiome(BlockPos pos) {
        return region.getBiome(pos);
    }

    public Levels getLevels() {
        return levels;
    }

    public TerraChunkGenerator getGenerator() {
        return generator;
    }

    public static DecorationContext of(ISeedReader world, ChunkGenerator generator) {
        if (generator instanceof TerraChunkGenerator && world instanceof RegionDelegate) {
            TerraChunkGenerator terraGenerator = (TerraChunkGenerator) generator;
            WorldGenRegion region = ((RegionDelegate) world).getDelegate();
            IChunk chunk = region.getChunk(region.getMainChunkX(), region.getMainChunkZ());
            if (chunk.getBiomes() instanceof TerraContainer) {
                TerraContainer container = (TerraContainer) chunk.getBiomes();
                return new DecorationContext(region, chunk, container, terraGenerator);
            }
        }
        return null;
    }
}
