/*
 *
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

package com.terraforged.mod.decorator.surface;

import com.terraforged.api.chunk.surface.Surface;
import com.terraforged.api.chunk.surface.SurfaceContext;
import com.terraforged.api.material.layer.LayerMaterial;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.mod.biome.provider.BiomeProvider;
import com.terraforged.mod.biome.provider.DesertBiomes;
import com.terraforged.mod.chunk.TerraContext;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.CellFunc;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class DesertDunes implements Surface {

    private final int maxHeight;
    private final Levels levels;
    private final Module module;
    private final Terrains terrains;
    private final DesertBiomes deserts;
    private final BlockPos.Mutable pos = new BlockPos.Mutable();

    public DesertDunes(TerraContext context, int maxHeight, DesertBiomes deserts) {
        Module dunes = Source.cell(context.seed.next(), 80, CellFunc.DISTANCE)
                .warp(context.seed.next(), 70, 1, 70);
        this.terrains = context.terrain;
        this.levels = context.levels;
        this.maxHeight = maxHeight;
        this.deserts = deserts;
        this.module = dunes;
    }

    @Override
    public void buildSurface(int x, int z, int surface, SurfaceContext ctx) {
        float value = module.getValue(x, z) * getMask(ctx.cell);
        float baseHeight = ctx.cell.value * levels.worldHeight;
        float duneHeight = baseHeight + value * maxHeight;
        int duneBase = (int) baseHeight;
        int duneTop = (int) duneHeight;
        if (duneTop < levels.waterLevel || duneTop <= baseHeight) {
            return;
        }

        LayerMaterial material = deserts.getSandLayers(ctx.biome);
        if (material == null) {
            return;
        }

        fill(x, z, duneBase, duneTop, ctx, ctx.chunk, material.getFull());

        float depth = material.getDepth(duneHeight);
        int levels = material.getLevel(depth);
        BlockState top = material.getState(levels);
        ctx.chunk.setBlockState(pos.setPos(x, duneTop, z), top, false);
    }

    public static Surface create(TerraContext context, BiomeProvider provider) {
        return create(context, provider.getModifierManager().getDesertBiomes());
    }

    public static Surface create(TerraContext context, DesertBiomes desertBiomes) {
        return new DesertDunes(context, 25, desertBiomes);
    }

    private static float getMask(Cell<Terrain> cell) {
        return cell.biomeMask(0F, 0.75F) * cell.mask(0.4F, 0.5F, 0F, 0.8F);
    }
}
