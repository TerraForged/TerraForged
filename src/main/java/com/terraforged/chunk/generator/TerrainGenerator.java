package com.terraforged.chunk.generator;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.api.chunk.surface.ChunkSurfaceBuffer;
import com.terraforged.api.chunk.surface.SurfaceContext;
import com.terraforged.chunk.TerraChunkGenerator;
import com.terraforged.chunk.column.ChunkPopulator;
import com.terraforged.chunk.fix.RegionFix;
import com.terraforged.chunk.util.FastChunk;
import com.terraforged.chunk.util.TerraContainer;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.feature.TerrainHelper;
import com.terraforged.util.Environment;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.INoiseGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.List;

public class TerrainGenerator {

    private final TerraChunkGenerator generator;
    private final TerrainHelper terrainHelper;
    private final INoiseGenerator surfaceNoise;

    public TerrainGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
        this.terrainHelper = new TerrainHelper(0.75F);
        this.surfaceNoise = new PerlinNoiseGenerator(new SharedSeedRandom(generator.getSeed()), 3, 0);
    }

    public final void generateTerrain(IWorld world, IChunk chunk) {
        TerraContainer container = getBiomeContainer(chunk);
        DecoratorContext context = new DecoratorContext(FastChunk.wrap(chunk), generator.getContext().levels, generator.getContext().terrain, generator.getContext().factory.getClimate());
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

    public final void generateSurface(WorldGenRegion world, IChunk chunk) {
        TerraContainer container = getBiomeContainer(chunk);
        ChunkSurfaceBuffer buffer = new ChunkSurfaceBuffer(FastChunk.wrap(chunk));
        SurfaceContext context = generator.getContext().surface(buffer, generator.getSettings());

        container.getChunkReader().iterate(context, (cell, dx, dz, ctx) -> {
            int px = ctx.blockX + dx;
            int pz = ctx.blockZ + dz;
            int top = ctx.chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, dx, dz);

            ctx.buffer.setSurfaceLevel(top);

            ctx.cell = cell;
            ctx.biome = container.getBiome(dx, dz);
            ctx.noise = getSurfaceNoise(px, pz) * 15D;

            generator.getSurfaceManager().getSurface(ctx).buildSurface(px, pz, top, ctx);

            int py = ctx.levels.scale(cell.value);
            for (ColumnDecorator processor : generator.getBaseDecorators()) {
                processor.decorate(ctx.buffer, ctx, px, py, pz);
            }
        });

        FastChunk.updateWGHeightmaps(chunk, context.pos);
    }

    public final void generateFeatures(WorldGenRegion region) {
        int chunkX = region.getMainChunkX();
        int chunkZ = region.getMainChunkZ();
        IChunk chunk = region.getChunk(chunkX, chunkZ);
        TerraContainer container = getBiomeContainer(chunk);
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

        // marks the heightmap data for this chunk for removal
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

    private double getSurfaceNoise(int x, int z) {
        double scale = 0.0625D;
        double noiseX = x * scale;
        double noiseZ = z * scale;
        double unusedValue1 = scale;
        double unusedValue2 = (x & 15) * scale;
        return surfaceNoise.noiseAt(noiseX, noiseZ, unusedValue1, unusedValue2);
    }

    private TerraContainer getBiomeContainer(IChunk chunk) {
        if (chunk.getBiomes() instanceof TerraContainer) {
            return (TerraContainer) chunk.getBiomes();
        }

        ChunkReader view = generator.getChunkReader(chunk.getPos().x, chunk.getPos().z);
        TerraContainer container = generator.getBiomeProvider().createBiomeContainer(view);
        if (chunk instanceof ChunkPrimer) {
            ((ChunkPrimer) chunk).func_225548_a_(container);
        } else if (chunk instanceof FastChunk) {
            ((FastChunk) chunk).setBiomes(container);
        }

        return container;
    }
}
