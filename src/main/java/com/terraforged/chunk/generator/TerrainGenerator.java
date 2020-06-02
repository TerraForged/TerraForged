package com.terraforged.chunk.generator;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.biome.provider.BiomeProvider;
import com.terraforged.chunk.TerraChunkGenerator;
import com.terraforged.chunk.column.ChunkPopulator;
import com.terraforged.chunk.fix.RegionFix;
import com.terraforged.chunk.util.FastChunk;
import com.terraforged.chunk.util.TerraContainer;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.feature.TerrainHelper;
import com.terraforged.util.Environment;
import com.terraforged.world.climate.Climate;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrains;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.List;

public class TerrainGenerator {

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

    public final void generateTerrain(IWorld world, IChunk chunk) {
        TerraContainer container = BiomeProvider.getBiomeContainer(generator, chunk);
        try (DecoratorContext context = new DecoratorContext(FastChunk.wrap(chunk), levels, terrain, climate)) {
            container.getChunkReader().iterate(context, (cell, dx, dz, ctx) -> {
                int px = ctx.blockX + dx;
                int pz = ctx.blockZ + dz;
                int py = ctx.levels.scale(cell.value);
                ctx.cell = cell;
                ctx.biome = container.getBiome(dx, dz);
                ChunkPopulator.INSTANCE.decorate(ctx.chunk, ctx, px, py, pz);
            });
            terrainHelper.flatten(world, chunk);
        }
    }

    public final void generateFeatures(WorldGenRegion region) {
        int chunkX = region.getMainChunkX();
        int chunkZ = region.getMainChunkZ();
        IChunk chunk = region.getChunk(chunkX, chunkZ);
        TerraContainer container = BiomeProvider.getBiomeContainer(generator, chunk);
        Biome biome = container.getFeatureBiome();
        DecoratorContext context = generator.getContext().decorator(chunk);

        IWorld regionFix = new RegionFix(region, generator);
        BlockPos pos = new BlockPos(context.blockX, 0, context.blockZ);

        // place biome features
        generator.getFeatureManager().decorate(generator, regionFix, chunk, biome, pos);

        // run post processes on chunk
        postProcess(container.getChunkReader(), container, context);

        // bake biome array & discard gen data
        ((ChunkPrimer) chunk).func_225548_a_(container.bakeBiomes(Environment.isVanillaBiomes()));

        container.getChunkReader().dispose();
    }

    private void postProcess(ChunkReader chunk, TerraContainer container, DecoratorContext context) {
        List<ColumnDecorator> decorators = generator.getPostProcessors();
        chunk.iterate(context, (cell, dx, dz, ctx) -> {
            int px = ctx.blockX + dx;
            int pz = ctx.blockZ + dz;
            int py = ctx.chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, dx, dz);
            ctx.cell = cell;
            ctx.biome = container.getBiome(dx, dz);
            for (ColumnDecorator decorator : decorators) {
                decorator.decorate(ctx.chunk, ctx, px, py, pz);
            }
        });
    }
}
