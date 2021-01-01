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

package com.terraforged.mod.server.command.search.condition;

import com.terraforged.engine.world.heightmap.Heightmap;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TerrainConditions {

    private static final Map<Terrain, SearchCondition.Factory> CONDITIONS = new ConcurrentHashMap<>();

    static {
        register(TerrainType.VOLCANO, VolcanoMatch::new);
        register(TerrainType.VOLCANO_PIPE, VolcanoMatch::new);
    }

    public static SearchCondition get(Terrain terrain, Heightmap heightmap) {
        SearchCondition.Factory factory = CONDITIONS.get(terrain);
        if (factory == null) {
            return new TerrainMatch(terrain, heightmap);
        }
        return factory.create(terrain, heightmap);
    }

    public static void register(Terrain terrain, SearchCondition.Factory factory) {
        CONDITIONS.putIfAbsent(terrain, factory);
    }
}
