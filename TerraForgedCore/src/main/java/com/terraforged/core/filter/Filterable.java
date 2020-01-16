package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Tag;

public interface Filterable<T extends Tag> {

    int getRawWidth();

    int getRawHeight();

    Cell<T> getCellRaw(int x, int z);

    Cell<T>[] getBacking();
}
