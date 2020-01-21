package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Tag;
import com.terraforged.core.region.Size;

public interface Filterable<T extends Tag> {

    Size getSize();

    Cell<T>[] getBacking();

    Cell<T> getCellRaw(int x, int z);
}
