package com.terraforged.feature.decorator.poisson;

import com.terraforged.chunk.fix.RegionDelegate;
import com.terraforged.chunk.util.TerraContainer;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.chunk.ChunkReader;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.WorldGenRegion;

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

    public static Module of(IWorld world, float fade) {
        if (world instanceof RegionDelegate) {
            WorldGenRegion region = ((RegionDelegate) world).getRegion();
            IChunk chunk = region.getChunk(region.getMainChunkX(), region.getMainChunkZ());
            return of(chunk, fade);
        }
        return Source.ONE;
    }

    public static Module of(IChunk chunk, float fade) {
        BiomeContainer container = chunk.getBiomes();
        if (container instanceof TerraContainer) {
            ChunkReader reader = ((TerraContainer) container).getChunkReader();
            return new BiomeVariance(reader, fade);
        }
        return Source.ONE;
    }
}
