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

package com.terraforged.chunk;

import com.terraforged.settings.StructureSettings;
import net.minecraft.world.gen.OverworldGenSettings;

public class TerraGenSettings extends OverworldGenSettings {

    private final int villageSeparation;
    private final int mansionSeparation;
    private final int biomeFeatureSeparation;
    private final int shipwreckDistance;
    private final int shipwreckSeparation;
    private final int oceanRuinDistance;
    private final int oceanRuinSeparation;

    public TerraGenSettings(StructureSettings settings) {
        super.villageDistance = settings.villages.distance;
        this.villageSeparation = settings.villages.separation;
        super.mansionDistance = settings.mansions.distance;
        this.mansionSeparation = settings.mansions.separation;
        super.strongholdDistance = settings.strongholds.distance;
        super.strongholdSpread = settings.strongholds.separation;
        super.oceanMonumentSpacing = settings.oceanMonuments.distance;
        super.oceanMonumentSeparation = settings.oceanMonuments.separation;
        super.biomeFeatureDistance = settings.otherStructures.distance;
        this.biomeFeatureSeparation = settings.otherStructures.separation;
        this.shipwreckDistance = settings.shipwrecks.distance;
        this.shipwreckSeparation = settings.shipwrecks.separation;
        this.oceanRuinDistance = settings.oceanRuins.distance;
        this.oceanRuinSeparation = settings.oceanRuins.separation;
    }

    @Override
    public int getVillageSeparation() {
        return villageSeparation;
    }

    @Override
    public int getMansionSeparation() {
        return mansionSeparation;
    }

    @Override
    public int getBiomeFeatureSeparation() {
        return biomeFeatureSeparation;
    }

    @Override
    public int getOceanRuinDistance() {
        return oceanRuinDistance;
    }

    @Override
    public int getOceanRuinSeparation() {
        return oceanRuinSeparation;
    }

    @Override
    public int getShipwreckDistance() {
        return shipwreckDistance;
    }

    @Override
    public int getShipwreckSeparation() {
        return shipwreckSeparation;
    }
}
