package com.terraforged.mod.worldgen.noise;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.continent.Continent;
import com.terraforged.engine.world.rivermap.Rivermap;

public class RiverCache {
    public final Cell cell = new Cell();
    protected long continentIndex;
    protected Rivermap rivermap;

    public Rivermap get(long continentIndex, Continent continent) {
        if (continentIndex != this.continentIndex || rivermap == null) {
            int conX = PosUtil.unpackLeft(continentIndex);
            int conZ = PosUtil.unpackRight(continentIndex);
            this.continentIndex = continentIndex;
            this.rivermap = continent.getRivermap(conX, conZ);
        }
        return rivermap;
    }
}
