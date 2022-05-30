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

package com.terraforged.mod.worldgen.noise.continent;

import com.terraforged.engine.world.heightmap.ControlPoints;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.worldgen.noise.continent.shape.FalloffPoint;

public interface ContinentPoints {
    float DEEP_OCEAN = 0.1f;
    float SHALLOW_OCEAN = 0.25f;
    float BEACH = 0.5f;
    float COAST = 0.55f;
    float INLAND = 0.6f;

    static Terrain getTerrainType(float continentNoise) {
        if (continentNoise < ContinentPoints.SHALLOW_OCEAN) {
            return TerrainType.DEEP_OCEAN;
        }
        if (continentNoise < ContinentPoints.BEACH) {
            return TerrainType.SHALLOW_OCEAN;
        }
        if (continentNoise < ContinentPoints.COAST) {
            return TerrainType.COAST;
        }
        return TerrainType.NONE;
    }

    static FalloffPoint[] getFalloff(ControlPoints controlPoints) {
        return new FalloffPoint[]{
                new FalloffPoint(controlPoints.inland, 1.0f, 1.0f),
                new FalloffPoint(controlPoints.coast, ContinentPoints.COAST, 1.0f),
                new FalloffPoint(controlPoints.beach, ContinentPoints.BEACH, ContinentPoints.COAST),
                new FalloffPoint(controlPoints.shallowOcean, ContinentPoints.SHALLOW_OCEAN, ContinentPoints.BEACH),
                new FalloffPoint(controlPoints.deepOcean, ContinentPoints.DEEP_OCEAN, ContinentPoints.SHALLOW_OCEAN),
        };
    }

    static float getFalloff(float continentNoise, FalloffPoint[] falloffCurve) {
        float previous = 1.0f;

        for (var falloff : falloffCurve) {

            if (continentNoise >= falloff.controlPoint()) {
                return MathUtil.map(continentNoise, falloff.controlPoint(), previous, falloff.min(), falloff.max());
            }

            previous = falloff.controlPoint();
        }

        return MathUtil.map(continentNoise, 0.0f, previous, 0.0f, ContinentPoints.DEEP_OCEAN);
    }
}
