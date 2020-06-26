package com.terraforged.mod.feature.decorator.poisson;

import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.util.NoiseUtil;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;

public class BiomeVariance implements Module {

    public static float MIN_FADE = 0.05F;

    private final ChunkReader chunk;
    private final float fade;
    private final float range;

    public BiomeVariance(ChunkReader chunk, float fade) {
        this.chunk = chunk;
        this.fade = fade;
        this.range = fade - MIN_FADE;
    }

    @Override
    public float getValue(float x, float y) {
        Cell cell = chunk.getCell((int) x, (int) y);
        return NoiseUtil.map(1 - cell.biomeEdge, MIN_FADE, fade, range);
    }

    public static Module of(IWorld world, ChunkGenerator<?> generator, float fade) {
        ChunkReader reader = TerraChunkGenerator.getChunk(world, generator);
        if (reader != null) {
            return new BiomeVariance(reader, fade);
        }
        return Source.ONE;
    }
}
