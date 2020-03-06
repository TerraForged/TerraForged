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

import com.terraforged.core.world.decorator.Decorator;
import com.terraforged.core.world.decorator.DesertStacks;
import com.terraforged.core.world.decorator.Wetlands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldDecorators {

    private final List<Decorator> decorators;

    public WorldDecorators(GeneratorContext context) {
        context = context.copy();
        List<Decorator> list = new ArrayList<>();
        list.add(new DesertStacks(context.seed, context.levels));
        list.add(new Wetlands(context.seed, context.terrain, context.levels));
        decorators = Collections.unmodifiableList(list);
    }

    public List<Decorator> getDecorators() {
        return decorators;
    }
}
