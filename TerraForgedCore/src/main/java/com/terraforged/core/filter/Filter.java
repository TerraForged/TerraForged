package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;

public interface Filter {

    void apply(Filterable<?> cellMap);

    default void setSeed(long seed) {

    }

    default void apply(Filterable<?> cellMap, int iterations) {
        while (iterations-- > 0) {
            apply(cellMap);
        }
    }

    default void iterate(Filterable<?> cellMap, Visitor visitor) {
        for (int dz = 0; dz < cellMap.getRawHeight(); dz++) {
            for (int dx = 0; dx < cellMap.getRawWidth(); dx++) {
                Cell<?> cell = cellMap.getCellRaw(dx, dz);
                visitor.visit(cellMap, cell, dx, dz);
            }
        }
    }

    interface Visitor {

        void visit(Filterable<?> cellMap, Cell cell, int dx, int dz);
    }
}
