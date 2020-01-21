package com.terraforged.core.filter;

import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.world.heightmap.Levels;

public class Smoothing implements Filter {

    private final int radius;
    private final float rad2;
    private final float strength;
    private final Modifier modifier;

    public Smoothing(Settings settings, Levels levels) {
        this.radius = NoiseUtil.round(settings.filters.smoothing.smoothingRadius + 0.5F);
        this.rad2 = settings.filters.smoothing.smoothingRadius * settings.filters.smoothing.smoothingRadius;
        this.strength = settings.filters.smoothing.smoothingRate;
        this.modifier = Modifier.range(levels.ground(10), levels.ground(150)).invert();
    }

    @Override
    public void apply(Filterable<?> map, int seedX, int seedZ, int iterations) {
        while (iterations-- > 0) {
            apply(map);
        }
    }

    private void apply(Filterable<?> cellMap) {
        int maxZ = cellMap.getSize().total - radius;
        int maxX = cellMap.getSize().total - radius;
        for (int z = radius; z < maxZ; z++) {
            for (int x = radius; x < maxX; x++) {
                Cell<?> cell = cellMap.getCellRaw(x, z);

                float total = 0;
                float weights = 0;

                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        float dist2 = dx * dx + dz * dz;
                        if (dist2 > rad2) {
                            continue;
                        }
                        int px = x + dx;
                        int pz = z + dz;
                        Cell<?> neighbour = cellMap.getCellRaw(px, pz);
                        if (neighbour.isAbsent()) {
                            continue;
                        }
                        float value = neighbour.value;
                        float weight = 1F - (dist2 / rad2);
                        total += (value * weight);
                        weights += weight;
                    }
                }

                if (weights > 0) {
                    float dif = cell.value - (total / weights);
//                    cell.value -= dif * strength;
                    cell.value -= modifier.modify(cell, dif * strength);
                }
            }
        }
    }
}
