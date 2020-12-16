/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.biome.surface;

import com.terraforged.api.biome.surface.MaskedSurface;
import com.terraforged.api.biome.surface.Surface;
import com.terraforged.api.biome.surface.SurfaceContext;
import com.terraforged.api.material.layer.LayerMaterial;
import com.terraforged.engine.cell.Cell;
import com.terraforged.mod.biome.provider.DesertBiomes;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.func.CellFunc;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.engine.world.heightmap.Levels;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;

public class DunesSurface implements MaskedSurface {

    private final int maxHeight;
    private final Levels levels;
    private final Module module;
    private final DesertBiomes deserts;
    private final BlockPos.Mutable pos = new BlockPos.Mutable();

    public DunesSurface(TerraContext context, int maxHeight, DesertBiomes deserts) {
        Module dunes = Source.cell(context.seed.next(), 80, CellFunc.DISTANCE)
                .warp(context.seed.next(), 70, 1, 70);
        this.levels = context.levels;
        this.maxHeight = maxHeight;
        this.deserts = deserts;
        this.module = dunes;
    }

    @Override
    public float getMask(Cell cell) {
        return NoiseUtil.map(cell.biomeRegionEdge, 0, 0.7F, 0.7F) * NoiseUtil.map(cell.riverMask, 0.5F, 0.95F, 0.45F);
    }

    @Override
    public void buildSurface(int x, int z, int surface, float mask, SurfaceContext ctx) {
        float value = module.getValue(x, z) * mask;
        float baseHeight = ctx.chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE_WG, x & 15, z & 15);
        float duneHeight = baseHeight + value * maxHeight;
        int duneBase = (int) baseHeight;
        int duneTop = (int) duneHeight + 1;
        if (duneTop < levels.waterLevel || duneTop <= baseHeight) {
            return;
        }

        LayerMaterial material = deserts.getSandLayers(ctx.biome);
        if (material == null) {
            fill(x, z, duneBase - 4, duneTop, ctx, ctx.chunk, Blocks.SAND.getDefaultState());
            return;
        }

        fill(x, z, duneBase, duneTop, ctx, ctx.chunk, material.getFull());

        float depth = material.getDepth(duneHeight);
        int levels = material.getLevel(depth);
        BlockState top = material.getState(levels);
        ctx.chunk.setBlockState(pos.setPos(x, duneTop, z), top, false);
    }

    public static Surface create(TerraContext context, TerraBiomeProvider provider) {
        return create(context, provider.getModifierManager().getDesertBiomes());
    }

    public static Surface create(TerraContext context, DesertBiomes desertBiomes) {
        return new DunesSurface(context, 25, desertBiomes);
    }
}
