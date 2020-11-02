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

package com.terraforged.mod.biome.utils;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.structure.*;

import java.util.Optional;
import java.util.function.Supplier;

public class Structures {

    public static Optional<? extends StructureFeature<?, ?>> getStructure(Biome biome, Structure<?> ref) {
        return biome.getGenerationSettings().getStructures().stream().map(Supplier::get).filter(f -> f.field_236268_b_ == ref).findFirst();
    }

    public static <C extends IFeatureConfig> Optional<C> getConfig(Biome biome, Structure<?> ref, Class<C> type) {
        return getStructure(biome, ref).map(f -> f.field_236269_c_).filter(type::isInstance).map(type::cast);
    }

    public static Structure<VillageConfig> outpost() {
        return Structure.PILLAGER_OUTPOST;
    }

    public static Structure<MineshaftConfig> mineshaft() {
        return Structure.MINESHAFT;
    }

    public static Structure<NoFeatureConfig> mansion() {
        return Structure.WOODLAND_MANSION;
    }

    public static Structure<NoFeatureConfig> jungleTemple() {
        return Structure.JUNGLE_PYRAMID;
    }

    public static Structure<NoFeatureConfig> desertTemple() {
        return Structure.DESERT_PYRAMID;
    }

    public static Structure<NoFeatureConfig> igloo() {
        return Structure.IGLOO;
    }

    public static Structure<RuinedPortalFeature> ruinedPortal() {
        return Structure.RUINED_PORTAL;
    }

    public static Structure<ShipwreckConfig> shipwreck() {
        return Structure.SHIPWRECK;
    }

    public static SwampHutStructure swampHut() {
        return Structure.SWAMP_HUT;
    }

    public static Structure<NoFeatureConfig> stronghold() {
        return Structure.STRONGHOLD;
    }

    public static Structure<NoFeatureConfig> oceanMonument() {
        return Structure.MONUMENT;
    }

    public static Structure<OceanRuinConfig> oceanRuin() {
        return Structure.OCEAN_RUIN;
    }

    public static Structure<NoFeatureConfig> fortress() {
        return Structure.FORTRESS;
    }

    public static Structure<NoFeatureConfig> endCity() {
        return Structure.END_CITY;
    }

    public static Structure<ProbabilityConfig> buriedTreasure() {
        return Structure.BURIED_TREASURE;
    }

    public static Structure<VillageConfig> village() {
        return Structure.VILLAGE;
    }

    public static Structure<NoFeatureConfig> netherFossil() {
        return Structure.NETHER_FOSSIL;
    }

    public static Structure<VillageConfig> bastionRemnant() {
        return Structure.BASTION_REMNANT;
    }

    public static void addDefaultMesaStructures(Biome biome) {
//        DefaultBiomeFeatures.func_235189_a_(biome);
    }

    public static void addMineshaftAndStronghold(Biome biome) {
//        DefaultBiomeFeatures.func_235196_b_(biome);
    }

    public static void addMineshaftAndRuinedPortal(Biome biome) {
//        DefaultBiomeFeatures.func_235196_b_(biome);
    }
}
