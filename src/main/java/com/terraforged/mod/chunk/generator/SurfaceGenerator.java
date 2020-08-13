package com.terraforged.mod.chunk.generator;

import com.terraforged.api.biome.surface.ChunkSurfaceBuffer;
import com.terraforged.api.biome.surface.SurfaceContext;
import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.util.FastChunk;
import com.terraforged.mod.chunk.util.TerraContainer;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.INoiseGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.stream.IntStream;

public class SurfaceGenerator implements Generator.Surfaces {

    private final TerraChunkGenerator generator;
    private final INoiseGenerator surfaceNoise;

    public SurfaceGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
        this.surfaceNoise = new PerlinNoiseGenerator(new SharedSeedRandom(generator.getSeed()), IntStream.rangeClosed(-3, 0));
    }

    @Override
    public final void generateSurface(WorldGenRegion world, IChunk chunk) {
        try (ChunkReader reader = generator.getChunkReader(chunk.getPos().x, chunk.getPos().z)) {
            TerraContainer container = TerraContainer.getOrCreate(chunk, reader, generator.getBiomeProvider());
            ChunkSurfaceBuffer buffer = new ChunkSurfaceBuffer(FastChunk.wrap(chunk));

            try (SurfaceContext context = generator.getContext().surface(buffer, generator.getSettings())) {
                reader.iterate(context, (cell, dx, dz, ctx) -> {
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
        }
    }

    private double getSurfaceNoise(int x, int z) {
        double scale = 0.0625D;
        double noiseX = x * scale;
        double noiseZ = z * scale;
        double unusedValue1 = scale;
        double unusedValue2 = (x & 15) * scale;
        return surfaceNoise.noiseAt(noiseX, noiseZ, unusedValue1, unusedValue2);
    }
}
