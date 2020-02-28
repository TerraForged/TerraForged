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

package com.terraforged.mod.chunk;

import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.api.chunk.surface.SurfaceContext;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.WorldGeneratorFactory;
import com.terraforged.core.world.heightmap.Heightmap;
import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.mod.material.Materials;
import com.terraforged.mod.settings.TerraSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationSettings;

public class TerraContext extends GeneratorContext {

    public final IWorld world;
    public final Heightmap heightmap;
    public final Materials materials;
    public final WorldGeneratorFactory factory;
    public final TerraSettings terraSettings;

    public TerraContext(IWorld world, Terrains terrain, TerraSettings settings) {
        super(terrain, settings, TerraTerrainProvider::new);
        this.world = world;
        this.materials = new Materials();
        this.terraSettings = settings;
        this.factory = new WorldGeneratorFactory(this);
        this.heightmap = factory.getHeightmap();
        ItemStack stack = new ItemStack(Items.COBBLESTONE);
        stack.getMaxStackSize();
    }

    public DecoratorContext decorator(IChunk chunk) {
        return new DecoratorContext(chunk, levels, terrain, factory.getClimate());
    }

    public SurfaceContext surface(IChunk chunk, GenerationSettings settings) {
        return new SurfaceContext(chunk, levels, terrain, factory.getClimate(), settings, world.getSeed());
    }
}
