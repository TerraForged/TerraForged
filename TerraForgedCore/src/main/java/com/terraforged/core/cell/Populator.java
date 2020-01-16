package com.terraforged.core.cell;

import me.dags.noise.Module;
import com.terraforged.core.util.concurrent.ObjectPool;
import com.terraforged.core.world.terrain.Terrain;

public interface Populator extends Module  {

    void apply(Cell<Terrain> cell, float x, float y);

    void tag(Cell<Terrain> cell, float x, float y);

    default float getValue(float x, float z) {
        try (ObjectPool.Item<Cell<Terrain>> cell = Cell.pooled()) {
            return getValue(cell.getValue(), x, z);
        }
    }

    default float getValue(Cell<Terrain> cell, float x, float z) {
        apply(cell, x, z);
        return cell.value;
    }
}
