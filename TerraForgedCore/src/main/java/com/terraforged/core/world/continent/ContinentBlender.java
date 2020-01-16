package com.terraforged.core.world.continent;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.module.Blender;
import com.terraforged.core.world.terrain.Terrain;

public class ContinentBlender extends Blender {

    private final Populator control;

    public ContinentBlender(Populator continent, Populator lower, Populator upper, float min, float max, float split, float tagThreshold) {
        super(continent, lower, upper, min, max, split, tagThreshold);
        this.control = continent;
    }

    @Override
    public float getValue(Cell<Terrain> cell, float x, float z) {
        return cell.continentEdge;
    }
}
