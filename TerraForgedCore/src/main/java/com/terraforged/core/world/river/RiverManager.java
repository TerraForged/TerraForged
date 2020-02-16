package com.terraforged.core.world.river;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.Cache;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.heightmap.Heightmap;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.util.NoiseUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RiverManager {

    private static final int QUAD_SIZE = (1 << RiverRegion.SCALE) / 2;

    private final LakeConfig lakes;
    private final RiverConfig primary;
    private final RiverConfig secondary;
    private final RiverConfig tertiary;
    private final Heightmap heightmap;
    private final GeneratorContext context;
    private final Cache<Long, RiverRegion> cache = new Cache<>(60, 60, TimeUnit.SECONDS, () -> new ConcurrentHashMap<>());

    public RiverManager(Heightmap heightmap, GeneratorContext context) {
        this.heightmap = heightmap;
        this.context = context;
        this.primary = RiverConfig.builder(context.levels)
                .bankHeight(context.settings.rivers.primaryRivers.minBankHeight, context.settings.rivers.primaryRivers.maxBankHeight)
                .bankWidth(context.settings.rivers.primaryRivers.bankWidth)
                .bedWidth(context.settings.rivers.primaryRivers.bedWidth)
                .bedDepth(context.settings.rivers.primaryRivers.bedDepth)
                .fade(context.settings.rivers.primaryRivers.fade)
                .length(2500)
                .main(true)
                .build();
        this.secondary = RiverConfig.builder(context.levels)
                .bankHeight(context.settings.rivers.secondaryRiver.minBankHeight, context.settings.rivers.secondaryRiver.maxBankHeight)
                .bankWidth(context.settings.rivers.secondaryRiver.bankWidth)
                .bedWidth(context.settings.rivers.secondaryRiver.bedWidth)
                .bedDepth(context.settings.rivers.secondaryRiver.bedDepth)
                .fade(context.settings.rivers.secondaryRiver.fade)
                .length(1000)
                .build();
        this.tertiary = RiverConfig.builder(context.levels)
                .bankHeight(context.settings.rivers.tertiaryRivers.minBankHeight, context.settings.rivers.tertiaryRivers.maxBankHeight)
                .bankWidth(context.settings.rivers.tertiaryRivers.bankWidth)
                .bedWidth(context.settings.rivers.tertiaryRivers.bedWidth)
                .bedDepth(context.settings.rivers.tertiaryRivers.bedDepth)
                .fade(context.settings.rivers.tertiaryRivers.fade)
                .length(500)
                .build();
        this.lakes = LakeConfig.of(context.settings.rivers.lake, context.levels);
    }

    public void apply(Cell<Terrain> cell, float x, float z) {
        int rx = RiverRegion.blockToRegion((int) x);
        int rz = RiverRegion.blockToRegion((int) z);

        // check which quarter of the region pos (x,y) is in & get the neighbouring regions' relative coords
        int qx = x < RiverRegion.regionToBlock(rx) + QUAD_SIZE ? -1 : 1;
        int qz = z < RiverRegion.regionToBlock(rz) + QUAD_SIZE ? -1 : 1;

        // relative positions of neighbouring regions
        int minX = Math.min(0, qx);
        int minZ = Math.min(0, qz);
        int maxX = Math.max(0, qx);
        int maxZ = Math.max(0, qz);

        for (int dz = minZ; dz <= maxZ; dz++) {
            for (int dx = minX; dx <= maxX; dx++) {
                getRegion(rx + dx, rz + dz).apply(cell, x, z);
            }
        }
    }

    private RiverRegion getRegion(int rx, int rz) {
        long id = NoiseUtil.seed(rx, rz);
        RiverRegion region = cache.get(id);
        if (region == null) {
            region = new RiverRegion(rx, rz, heightmap, context, primary, secondary, tertiary, lakes);
            cache.put(id, region);
        }
        return region;
    }
}
