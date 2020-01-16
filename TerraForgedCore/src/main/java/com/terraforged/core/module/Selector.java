package com.terraforged.core.module;

import me.dags.noise.Module;
import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.TerrainPopulator;

import java.util.LinkedList;
import java.util.List;

public class Selector implements Populator {

    private final int maxIndex;
    private final Module control;
    private final Populator[] nodes;

    public Selector(Module control, List<Populator> populators) {
        this.control = control;
        this.nodes = getWeightedArray(populators);
        this.maxIndex = nodes.length - 1;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float y) {
        get(x, y).apply(cell, x, y);
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float y) {
        get(x, y).tag(cell, x, y);
    }

    public Populator get(float x, float y) {
        float selector = control.getValue(x, y);
        int index = NoiseUtil.round(selector * maxIndex);
        return nodes[index];
    }

    private static Populator[] getWeightedArray(List<Populator> modules) {
        float smallest = Float.MAX_VALUE;
        for (Populator p : modules) {
            if (p instanceof TerrainPopulator) {
                smallest = Math.min(smallest, ((TerrainPopulator) p).getType().getWeight());
            } else {
                smallest = Math.min(smallest, 1);
            }
        }

        List<Populator> result = new LinkedList<>();
        for (Populator p : modules) {
            int count;
            if (p instanceof TerrainPopulator) {
                count = Math.round(((TerrainPopulator) p).getType().getWeight() / smallest);
            } else {
                count = Math.round(1 / smallest);
            }
            while (count-- > 0) {
                result.add(p);
            }
        }

        return result.toArray(new Populator[0]);
    }
}
