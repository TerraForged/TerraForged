package com.terraforged.biome.provider;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.TerrainType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class BiomeAccess {

    private static final BiomeAccessor DEEP_OCEAN = (m, c) -> m.getDeepOcean(c.temperature, c.moisture, c.biome);
    private static final BiomeAccessor SHALLOW_OCEAN = (m, c) -> m.getOcean(c.temperature, c.moisture, c.biome);
    private static final BiomeAccessor LAKE = (m, c) -> m.getLake(c.temperature, c.moisture, c.biome);
    private static final BiomeAccessor WETLAND = (m, c) -> m.getWetland(c.temperature, c.moisture, c.biome);
    private static final BiomeAccessor NORMAL = (m, c) -> m.getBiome(c.biomeType, c.temperature, c.moisture, c.biome);
    private static final BiomeAccessor RIVER = (m, c) -> {
        Biome biome = m.getBiome(c.biomeType, c.temperature, c.moisture, c.biome);
        if (overridesRiver(biome)) {
            return biome;
        }
        return m.getRiver(c.temperature, c.moisture, c.biome);
    };

    private static final BiomeAccessor[] accessors = createAccessorArray();

    private final Levels levels;

    public BiomeAccess(Levels levels) {
        this.levels = levels;
    }

    public BiomeAccessor getAccessor(Cell cell) {
        TerrainType type = cell.terrain.getType();
        if (type.isSubmerged() && cell.value > levels.water) {
            return NORMAL;
        }
        return accessors[cell.terrain.getType().ordinal()];
    }

    private static BiomeAccessor[] createAccessorArray() {
        BiomeAccessor[] accessors = new BiomeAccessor[TerrainType.values().length];
        accessors[TerrainType.SHALLOW_OCEAN.ordinal()] = SHALLOW_OCEAN;
        accessors[TerrainType.DEEP_OCEAN.ordinal()] = DEEP_OCEAN;
        accessors[TerrainType.WETLAND.ordinal()] = WETLAND;
        accessors[TerrainType.RIVER.ordinal()] = RIVER;
        accessors[TerrainType.LAKE.ordinal()] = LAKE;
        for (TerrainType type : TerrainType.values()) {
            if (accessors[type.ordinal()] == null) {
                accessors[type.ordinal()] = NORMAL;
            }
        }
        return accessors;
    }

    private static boolean overridesRiver(Biome biome) {
        return biome.getCategory() == Biome.Category.SWAMP || biome.getCategory() == Biome.Category.JUNGLE;
    }
}
