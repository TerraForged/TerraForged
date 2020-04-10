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

package com.terraforged.core.world.rivermap.river;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.TerrainPopulator;
import com.terraforged.core.world.terrain.Terrains;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.source.Line;
import me.dags.noise.util.NoiseUtil;

public class River extends TerrainPopulator {

    public static final int VALLEY_WIDTH = 275;
    protected static final float DEPTH_FADE_STRENGTH = 0.5F;

    public final boolean main;
    private final boolean connecting;

    private final float bedHeight;

    private final float minBankHeight;
    private final float maxBankHeight;
    private final float bankAlphaMin;
    private final float bankAlphaMax;
    private final float bankAlphaRange;
    private final Module bankVariance;

    private final Line bed;
    private final Line banks;
    private final Line valley;
    public final RiverConfig config;
    public final RiverBounds bounds;

    private final Terrains terrains;

    private final float depthFadeBias;

    public River(RiverBounds bounds, RiverConfig config, Terrains terrains, double fadeIn, double fadeOut) {
        this(bounds, config, terrains, fadeIn, fadeOut, false);
    }

    public River(RiverBounds bounds, RiverConfig config, Terrains terrains, double fadeIn, double fadeOut, boolean connecting) {
        super(Source.ZERO, terrains.river);
        Module in = Source.constant(fadeIn);
        Module out = Source.constant(fadeOut);
        Module bedWidth = Source.constant(config.bedWidth * config.bedWidth);
        Module bankWidth = Source.constant(config.bankWidth * config.bankWidth);
        Module valleyWidth = Source.constant(VALLEY_WIDTH * VALLEY_WIDTH);
        this.bounds = bounds;
        this.config = config;
        this.main = config.main;
        this.terrains = terrains;
        this.connecting = connecting;
        this.bedHeight = config.bedHeight;
        this.minBankHeight = config.minBankHeight;
        this.maxBankHeight = config.maxBankHeight;
        this.bankAlphaMin = minBankHeight;
        this.bankAlphaMax = Math.min(1, minBankHeight + 0.35F);
        this.bankAlphaRange = bankAlphaMax - bankAlphaMin;
        this.bankVariance = Source.perlin(1234, 150, 1);
        this.depthFadeBias = 1 - DEPTH_FADE_STRENGTH;
        this.bed = Source.line(bounds.x1(), bounds.y1(), bounds.x2(), bounds.y2(), bedWidth, in, out, 0.1F);
        this.banks = Source.line(bounds.x1(), bounds.y1(), bounds.x2(), bounds.y2(), bankWidth, in, out, 0.1F);
        this.valley = Source.line(bounds.x1(), bounds.y1(), bounds.x2(), bounds.y2(), valleyWidth, Source.ZERO, Source.ZERO, 0.33F);
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float z) {
        if (cell.value <= bedHeight) {
            return;
        }
        carve(cell, x, z);
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float z) {
        if (!terrains.overridesRiver(cell.tag)) {
            cell.tag = terrains.river;
        }
    }

    private void carve(Cell<Terrain> cell, float x, float z) {
        float valleyAlpha = valley.getValue(x, z);
        if (valleyAlpha == 0) {
            return;
        }

        // riverMask decreases the closer to the river the position gets
        cell.riverMask *= (1 - valleyAlpha);

        float bankHeight = getBankHeight(cell, x, z);
        if (!carveValley(cell, valleyAlpha, bankHeight)) {
            return;
        }

        // is a branching river and x,z is past the connecting point
        if (connecting && banks.clipEnd(x, z)) {
            return;
        }

        float widthModifier = banks.getWidthModifier(x, z);
        float banksAlpha = banks.getValue(x, z, widthModifier);
        if (banksAlpha == 0) {
            return;
        }

        float bedHeight = getBedHeight(bankHeight, widthModifier);
        if (!carveBanks(cell, banksAlpha, bedHeight)) {
            return;
        }

        float bedAlpha = bed.getValue(x, z);
        if (bedAlpha == 0) {
            return;
        }

        carveBed(cell, bedHeight);
    }

    private float getBankHeight(Cell<Terrain> cell, float x, float z) {
        // scale bank height based on elevation of the terrain (higher terrain == taller banks)
        float bankHeightAlpha = NoiseUtil.map(cell.value, bankAlphaMin, bankAlphaMax, bankAlphaRange);
        // use perlin noise to add a little extra variance to the bank height
        float bankHeightVariance = bankVariance.getValue(x, z);
        // lerp between the min and max heights
        return NoiseUtil.lerp(minBankHeight, maxBankHeight, bankHeightAlpha * bankHeightVariance);
    }

    private float getBedHeight(float bankHeight, float widthModifier) {
        // scale depth of river by with it's width (wider == deeper)
        // depthAlpha changes the river depth up ${DEPTH_FADE_STRENGTH} %
        float depthAlpha = depthFadeBias + (DEPTH_FADE_STRENGTH * widthModifier);
        return NoiseUtil.lerp(bankHeight, this.bedHeight, depthAlpha);
    }

    private boolean carveValley(Cell<Terrain> cell, float valleyAlpha, float bankHeight) {
        // lerp the position's height to the riverbank height
        if (cell.value > bankHeight) {
            cell.value = NoiseUtil.lerp(cell.value, bankHeight, valleyAlpha);
            return true;
        }
        return false;
    }

    private boolean carveBanks(Cell<Terrain> cell, float banksAlpha, float bedHeight) {
        // lerp the position's height to the riverbed height (ie the riverbank slopes)
        if (cell.value > bedHeight) {
            cell.value = NoiseUtil.lerp(cell.value, bedHeight, banksAlpha);
            tag(cell, terrains.riverBanks);
            return true;
        }
        return false;
    }

    private void carveBed(Cell<Terrain> cell, float bedHeight) {
        if (cell.value > bedHeight) {
            cell.value = bedHeight;
        }
        tag(cell, terrains.river);
    }


    private void tag(Cell<Terrain> cell, Terrain tag) {
        if (!terrains.overridesRiver(cell.tag)) {
            cell.tag = tag;
        }
    }

    public static boolean validStart(float value) {
        return value > (70F / 256F);
    }

    public static boolean validEnd(float value) {
        return value < (60F / 256F);
    }
}
