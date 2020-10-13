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
        return biome.func_242440_e().func_242487_a().stream().map(Supplier::get).filter(f -> f.field_236268_b_ == ref).findFirst();
    }

    public static <C extends IFeatureConfig> Optional<C> getConfig(Biome biome, Structure<?> ref, Class<C> type) {
        return getStructure(biome, ref).map(f -> f.field_236269_c_).filter(type::isInstance).map(type::cast);
    }

    public static Structure<VillageConfig> outpost() {
        return Structure.field_236366_b_;
    }

    public static Structure<MineshaftConfig> mineshaft() {
        return Structure.field_236367_c_;
    }

    public static Structure<NoFeatureConfig> mansion() {
        return Structure.field_236368_d_;
    }

    public static Structure<NoFeatureConfig> jungleTemple() {
        return Structure.field_236369_e_;
    }

    public static Structure<NoFeatureConfig> desertTemple() {
        return Structure.field_236370_f_;
    }

    public static Structure<NoFeatureConfig> igloo() {
        return Structure.field_236371_g_;
    }

    public static Structure<RuinedPortalFeature> ruinedPortal() {
        return Structure.field_236372_h_;
    }

    public static Structure<ShipwreckConfig> shipwreck() {
        return Structure.field_236373_i_;
    }

    public static SwampHutStructure swampHut() {
        return Structure.field_236374_j_;
    }

    public static Structure<NoFeatureConfig> stronghold() {
        return Structure.field_236375_k_;
    }

    public static Structure<NoFeatureConfig> oceanMonument() {
        return Structure.field_236376_l_;
    }

    public static Structure<OceanRuinConfig> oceanRuin() {
        return Structure.field_236377_m_;
    }

    public static Structure<NoFeatureConfig> fortress() {
        return Structure.field_236378_n_;
    }

    public static Structure<NoFeatureConfig> endCity() {
        return Structure.field_236379_o_;
    }

    public static Structure<ProbabilityConfig> buriedTreasure() {
        return Structure.field_236380_p_;
    }

    public static Structure<VillageConfig> village() {
        return Structure.field_236381_q_;
    }

    public static Structure<NoFeatureConfig> netherFossil() {
        return Structure.field_236382_r_;
    }

    public static Structure<VillageConfig> bastionRemnant() {
        return Structure.field_236383_s_;
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
