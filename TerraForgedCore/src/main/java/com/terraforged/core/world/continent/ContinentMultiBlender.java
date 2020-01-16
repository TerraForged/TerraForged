package com.terraforged.core.world.continent;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.module.MultiBlender;
import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.terrain.Terrain;

public class ContinentMultiBlender extends MultiBlender {

    private final Populator control;

    public ContinentMultiBlender(Climate climate, Populator continent, Populator lower, Populator middle, Populator upper, float min, float mid, float max) {
        super(climate, continent, lower, middle, upper, min, mid, max);
        this.control = continent;
    }

    @Override
    public float getValue(Cell<Terrain> cell, float x, float z) {
        return cell.continentEdge;
    }
}
