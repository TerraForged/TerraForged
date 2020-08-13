package com.terraforged.mod.biome.map.defaults;

import com.terraforged.core.cell.Cell;
import com.terraforged.fm.GameContext;
import net.minecraft.world.biome.Biome;

public interface DefaultBiome {

    Biome getBiome(float temperature);

    default Biome getNone() {
        return null;
    }

    default Biome getDefaultBiome(Cell cell) {
        return getBiome(cell.temperature);
    }

    default Biome getDefaultBiome(float temperature) {
        return getBiome(temperature);
    }

    interface Factory {

        DefaultBiome create(GameContext context);
    }
}
