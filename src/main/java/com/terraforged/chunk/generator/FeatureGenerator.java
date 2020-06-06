package com.terraforged.chunk.generator;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.chunk.TerraChunkGenerator;
import com.terraforged.chunk.fix.RegionFix;
import com.terraforged.chunk.util.TerraContainer;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.util.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.List;

public class FeatureGenerator {

    private final TerraChunkGenerator generator;

    public FeatureGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    public final void generateFeatures(WorldGenRegion region) {
        int chunkX = region.getMainChunkX();
        int chunkZ = region.getMainChunkZ();
        IChunk chunk = region.getChunk(chunkX, chunkZ);

        ChunkReader reader = generator.getChunkReader(chunkX, chunkZ);
        TerraContainer container = TerraContainer.getOrCreate(chunk, reader, generator.getBiomeProvider());

        Biome biome = container.getFeatureBiome(reader);
        DecoratorContext context = generator.getContext().decorator(chunk);

        IWorld regionFix = new RegionFix(region, generator);
        BlockPos pos = new BlockPos(context.blockX, 0, context.blockZ);

        // place biome features
        generator.getFeatureManager().decorate(generator, regionFix, chunk, biome, pos);

        // run post processes on chunk
        postProcess(reader, container, context);

        // bake biome array
        ((ChunkPrimer) chunk).func_225548_a_(container.bakeBiomes(Environment.isVanillaBiomes()));

        // close the current chunk reader
        reader.close();

        // mark chunk disposed as this is the last usage of the reader
        reader.dispose();
    }

    private void postProcess(ChunkReader reader, TerraContainer container, DecoratorContext context) {
        List<ColumnDecorator> decorators = generator.getPostProcessors();
        reader.iterate(context, (cell, dx, dz, ctx) -> {
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
