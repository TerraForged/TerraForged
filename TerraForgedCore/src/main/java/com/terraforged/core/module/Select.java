package com.terraforged.core.module;

import me.dags.noise.Module;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;

public class Select {

    private final Module control;

    public Select(Module control) {
        this.control = control;
    }

    public float getSelect(Cell<Terrain> cell, float x, float y) {
        return control.getValue(x, y);
    }
}
