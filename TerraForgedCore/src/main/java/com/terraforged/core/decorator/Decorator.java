package com.terraforged.core.decorator;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;

public interface Decorator {

    void apply(Cell<Terrain> cell, float x, float y);
}
