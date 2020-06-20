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

package com.terraforged.chunk.settings;

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;
import net.minecraft.world.gen.OverworldGenSettings;

@Serializable
public class StructureSettings {

    private static final StructureDefaults defaults = new StructureDefaults();

    public Structure villages = new Structure(defaults.village_dist, defaults.village_sep);

    public Structure mansions = new Structure(defaults.mansionDist, defaults.mansionSep);

    public Structure strongholds = new Structure(defaults.strongholdDist, defaults.strongholdSep);

    public Structure shipwrecks = new Structure(defaults.shipWreckDist, defaults.shipWreckSep);

    public Structure oceanRuins = new Structure(defaults.oceanRuinDist, defaults.oceanRuinSep);

    public Structure oceanMonuments = new Structure(defaults.oceanMonumentDist, defaults.oceanMonumentSep);

    public Structure otherStructures = new Structure(defaults.otherStructureDist, defaults.otherStructureSep);

    @Serializable
    public static class Structure {

        @Range(min = 1, max = 200)
        @Comment("The distance (in chunks) between placements of this feature")
        public int distance;

        @Range(min = 1, max = 50)
        @Comment("The separation (in chunks) between placements of this feature")
        public int separation;

        public Structure() {

        }

        public Structure(int distance, int separation) {
            this.distance = distance;
            this.separation = separation;
        }
    }

    private static class StructureDefaults {

        public final int village_dist;
        public final int village_sep;
        public final int mansionDist;
        public final int mansionSep;
        public final int strongholdDist;
        public final int strongholdSep;
        public final int shipWreckDist;
        public final int shipWreckSep;
        public final int oceanRuinDist;
        public final int oceanRuinSep;
        public final int oceanMonumentDist;
        public final int oceanMonumentSep;
        public final int otherStructureDist;
        public final int otherStructureSep;

        public StructureDefaults() {
            OverworldGenSettings defaults = new OverworldGenSettings();
            village_dist = (int) (defaults.getVillageDistance() * 1.5);
            village_sep = (int) (defaults.getVillageSeparation() * 1.5);

            shipWreckDist = (int) (defaults.getShipwreckDistance() * 2.0);
            shipWreckSep = (int) (defaults.getShipwreckSeparation() * 2.0);

            oceanRuinDist = (int) (defaults.getOceanRuinDistance() * 2.0);
            oceanRuinSep = (int) (defaults.getOceanRuinSeparation() * 2.0);

            oceanMonumentDist = (int) (defaults.getOceanRuinDistance() * 1.5);
            oceanMonumentSep = (int) (defaults.getOceanRuinSeparation() * 1.5);

            mansionDist = defaults.getMansionDistance();
            mansionSep = defaults.getMansionSeparation();

            strongholdDist = defaults.getMansionSeparation();
            strongholdSep = defaults.getMansionSeparation();

            otherStructureDist = defaults.getBiomeFeatureDistance();
            otherStructureSep = defaults.getBiomeFeatureSeparation();
        }
    }
}
