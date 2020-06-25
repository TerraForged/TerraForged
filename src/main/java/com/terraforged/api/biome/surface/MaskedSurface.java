package com.terraforged.api.biome.surface;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.util.NoiseUtil;

public interface MaskedSurface extends Surface {

    default float getMask(Cell cell) {
        return cell.biomeEdge * NoiseUtil.map(cell.riverMask,0, 0.0005F, 0.0005F);
    }

    @Override
    default void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        buildSurface(x, z, height, getMask(ctx.cell), ctx);
    }

    void buildSurface(int x, int z, int height, float mask, SurfaceContext ctx);
}
