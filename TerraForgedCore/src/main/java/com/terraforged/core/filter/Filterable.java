package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Tag;

public interface Filterable<T extends Tag> {

    int getRawSize();

    Cell<T>[] getBacking();

    Cell<T> getCellRaw(int x, int z);
}
