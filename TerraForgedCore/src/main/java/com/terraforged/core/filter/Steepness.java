package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrains;

public class Steepness implements Filter, Filter.Visitor {

    private final int radius;
    private final float scaler;
    private final Terrains terrains;

    public Steepness(Terrains terrains) {
        this(1, 16F, terrains);
    }

    public Steepness(int radius, float scaler, Terrains terrains) {
        this.radius = radius;
        this.scaler = scaler;
        this.terrains = terrains;
    }

    @Override
    public void apply(Filterable<?> cellMap) {
        iterate(cellMap, this);
    }

    @Override
    public void visit(Filterable<?> cellMap, Cell cell, int cx, int cz) {
        float totalHeightDif = 0F;
        for (int dz = -1; dz <= 2; dz++) {
            for (int dx = -1; dx <= 2; dx++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }

                int x = cx + dx * radius;
                int z = cz + dz * radius;
                Cell<?> neighbour = cellMap.getCellRaw(x, z);
                if (neighbour.isAbsent()) {
                    continue;
                }

                float height = Math.max(neighbour.value, 62 / 256F);

                totalHeightDif += (Math.abs(cell.value - height) / radius);
            }
        }
        cell.steepness = Math.min(1, totalHeightDif * scaler);
        if (cell.tag == terrains.coast && cell.steepness < 0.22F) {
            cell.tag = terrains.beach;
        }
    }
}
