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

package com.terraforged.core.world;

import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.heightmap.Heightmap;
import com.terraforged.core.world.heightmap.WorldHeightmap;

import java.util.function.Supplier;

public class WorldGeneratorFactory implements Supplier<WorldGenerator> {

    private final GeneratorContext context;

    private final Heightmap heightmap;
    private final WorldDecorators decorators;

    public WorldGeneratorFactory(GeneratorContext context) {
        this.context = context;
        this.heightmap = new WorldHeightmap(context);
        this.decorators = new WorldDecorators(context);
    }

    public WorldGeneratorFactory(GeneratorContext context, Heightmap heightmap) {
        this.context = context;
        this.heightmap = heightmap;
        this.decorators = new WorldDecorators(context);
    }

    public Heightmap getHeightmap() {
        return heightmap;
    }

    public Climate getClimate() {
        return getHeightmap().getClimate();
    }

    public WorldDecorators getDecorators() {
        return decorators;
    }

    @Override
    public WorldGenerator get() {
        return new WorldGenerator(heightmap, decorators, new WorldFilters(context));
    }
}
