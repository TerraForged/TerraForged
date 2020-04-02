package com.terraforged.mod.feature.decorator.poisson;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.mod.chunk.TerraContainer;
import com.terraforged.mod.chunk.fix.RegionDelegate;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.WorldGenRegion;

public class BiomeVariance implements Module {

    private final ChunkReader chunk;

    public BiomeVariance(ChunkReader chunk) {
        this.chunk = chunk;
    }

    @Override
    public float getValue(float x, float y) {
        int dx = ((int) x) & 15;
        int dz = ((int) y) & 15;
        Cell<?> cell = chunk.getCell(dx, dz);
        return NoiseUtil.map(1 - cell.biomeEdge, 0.05F, 0.25F, 0.2F);
    }

    public static Module of(IWorld world) {
        if (world instanceof RegionDelegate) {
            WorldGenRegion region = ((RegionDelegate) world).getRegion();
            IChunk chunk = region.getChunk(region.getMainChunkX(), region.getMainChunkZ());
            return of(chunk);
        }
        return Source.ONE;
    }

    public static Module of(IChunk chunk) {
        BiomeContainer container = chunk.getBiomes();
        if (container instanceof TerraContainer) {
            ChunkReader reader = ((TerraContainer) container).getChunkReader();
            return new BiomeVariance(reader);
        }
        return Source.ONE;
    }
}
