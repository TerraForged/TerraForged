/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.worldgen.asset;

import com.mojang.serialization.Codec;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainHelper;
import com.terraforged.mod.codec.LazyCodec;
import com.terraforged.mod.registry.ModRegistry;

import java.util.function.Supplier;

public class TerrainType {
    public static final TerrainType NONE = new TerrainType("none", com.terraforged.engine.world.terrain.TerrainType.NONE);

    public static final Codec<TerrainType> DIRECT = LazyCodec.record(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(TerrainType::getName),
            Codec.STRING.fieldOf("parent").xmap(TerrainType::forName, Terrain::getName).forGetter(TerrainType::getParentType)
    ).apply(instance, TerrainType::new));

    public static final Codec<Supplier<TerrainType>> CODEC = LazyCodec.registry(DIRECT, ModRegistry.TERRAIN_TYPE);

    private final String name;
    private final Terrain parentType;
    private final Terrain terrain;

    public TerrainType(String name, Terrain type) {
        this.name = name;
        this.parentType = type;
        this.terrain = TerrainHelper.getOrCreate(name, type);
    }

    public String getName() {
        return name;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public Terrain getParentType() {
        return parentType;
    }

    private static Terrain forName(String name) {
        return com.terraforged.engine.world.terrain.TerrainType.get(name);
    }

    public static TerrainType of(Terrain terrain) {
        if (terrain.getDelegate() instanceof Terrain parent) {
            return new TerrainType(terrain.getName(), parent);
        }
        return new TerrainType(terrain.getName(), terrain);
    }
}
