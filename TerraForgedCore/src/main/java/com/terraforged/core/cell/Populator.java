package com.terraforged.core.cell;

import com.terraforged.core.util.concurrent.ObjectPool;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.Module;

public interface Populator extends Module {

    void apply(Cell<Terrain> cell, float x, float y);

    void tag(Cell<Terrain> cell, float x, float y);

    @Override
    default float getValue(float x, float z) {
        try (ObjectPool.Item<Cell<Terrain>> cell = Cell.pooled()) {
            apply(cell.getValue(), x, z);
            return cell.getValue().value;
        }
    }
}
