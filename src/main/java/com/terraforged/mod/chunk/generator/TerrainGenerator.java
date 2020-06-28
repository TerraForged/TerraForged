package com.terraforged.mod.chunk.generator;

import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.column.BaseDecorator;
import com.terraforged.mod.chunk.util.FastChunk;
import com.terraforged.mod.chunk.util.TerraContainer;
import com.terraforged.mod.feature.TerrainHelper;
import com.terraforged.world.climate.Climate;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrains;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;

public class TerrainGenerator implements Generator.Terrain {

    private final Levels levels;
    private final Climate climate;
    private final Terrains terrain;
    private final TerraChunkGenerator generator;
    private final TerrainHelper terrainHelper;

    public TerrainGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
        this.levels = generator.getContext().levels;
        this.terrain = generator.getContext().terrain;
        this.climate = generator.getContext().factory.getClimate();
        this.terrainHelper = new TerrainHelper(0.75F);
    }

    @Override
    public final void generateTerrain(IWorld world, IChunk chunk) {
        try (ChunkReader reader = generator.getChunkReader(chunk.getPos().x, chunk.getPos().z)) {
            TerraContainer container = TerraContainer.getOrCreate(chunk, reader, generator.getBiomeProvider());
            try (DecoratorContext context = new DecoratorContext(FastChunk.wrap(chunk), levels, terrain, climate)) {
                reader.iterate(context, (cell, dx, dz, ctx) -> {
                    int px = ctx.blockX + dx;
                    int pz = ctx.blockZ + dz;
                    int py = ctx.levels.scale(cell.value);
                    ctx.cell = cell;
                    ctx.biome = container.getNoiseBiome(dx, world.getSeaLevel(), dz);
                    BaseDecorator.INSTANCE.decorate(ctx.chunk, ctx, px, py, pz);
                });
                terrainHelper.flatten(world, chunk);
            }
        }
    }
}
