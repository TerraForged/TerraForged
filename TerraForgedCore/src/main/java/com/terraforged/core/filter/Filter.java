package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;

public interface Filter {

    void apply(Filterable<?> map, int seedX, int seedZ, int iterations);

    default void iterate(Filterable<?> map, Visitor visitor) {
        for (int dz = 0; dz < map.getSize().total; dz++) {
            for (int dx = 0; dx < map.getSize().total; dx++) {
                Cell<?> cell = map.getCellRaw(dx, dz);
                visitor.visit(map, cell, dx, dz);
            }
        }
    }

    interface Visitor {

        void visit(Filterable<?> cellMap, Cell cell, int dx, int dz);
    }
}
