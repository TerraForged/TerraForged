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

package com.terraforged.mod.data.gen.feature;

import com.mojang.datafixers.util.Pair;
import com.terraforged.fm.GameContext;
import com.terraforged.fm.data.FeatureInjectorProvider;
import com.terraforged.fm.matcher.biome.BiomeMatcher;
import com.terraforged.fm.matcher.feature.FeatureMatcher;
import com.terraforged.fm.modifier.Jsonifiable;
import com.terraforged.fm.modifier.Modifier;
import com.terraforged.fm.template.TemplateManager;
import com.terraforged.fm.transformer.FeatureReplacer;
import com.terraforged.mod.feature.TerraFeatures;
import com.terraforged.mod.feature.context.ContextSelectorConfig;
import com.terraforged.mod.feature.context.ContextSelectorFeature;
import com.terraforged.mod.feature.context.ContextualFeature;
import com.terraforged.mod.feature.context.modifier.Biome;
import com.terraforged.mod.feature.context.modifier.Elevation;
import com.terraforged.mod.feature.decorator.poisson.PoissonAtSurface;
import com.terraforged.mod.feature.decorator.poisson.PoissonConfig;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Trees {

    public static void addInjectors(FeatureInjectorProvider provider) {
        provider.add("trees/oak_badlands", oakBadlands(provider.getContext()));
        provider.add("trees/oak_forest", oakForest(provider.getContext()));
        provider.add("trees/oak_plains", oakPlains(provider.getContext()));
        provider.add("trees/birch", birch(provider.getContext()));
        provider.add("trees/birch_oak", birchOak(provider.getContext()));
        provider.add("trees/dark_oak", darkForest(provider.getContext()));
        provider.add("trees/flower_plains", flowerForest(provider.getContext()));
        provider.add("trees/jungle", jungle(provider.getContext()));
        provider.add("trees/jungle_edge", jungleEdge(provider.getContext()));
        provider.add("trees/jungle_hills", jungleHills(provider.getContext()));
        provider.add("trees/pine", pine(provider.getContext()));
        provider.add("trees/redwood", redwood(provider.getContext()));
        provider.add("trees/spruce", spruce(provider.getContext()));
        provider.add("trees/spruce_tundra", spruceTundra(provider.getContext()));
    }

    private static Modifier<Jsonifiable> oakBadlands(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(
                        context,
                        "minecraft:wooded_badlands*",
                        "minecraft:shattered_savanna*",
                        "minecraft:modified_badlands*",
                        "minecraft:modified_wooded*"
                ),
                FeatureMatcher.and(Blocks.OAK_LOG, Blocks.OAK_LEAVES),
                FeatureReplacer.of(extra(
                        0, 0.02F, 3,
                        "terraforged:oak_small",
                        Pair.of("terraforged:oak_small", 0.2F),
                        Pair.of("terraforged:acacia_bush", 0.1F)
                ))
        );
    }

    private static Modifier<Jsonifiable> oakForest(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:forest"),
                FeatureMatcher.and(Blocks.OAK_LOG, Blocks.OAK_LEAVES),
                FeatureReplacer.of(poisson(
                        8, 0.12F, 150, 0.5F, 1.75F,
                        "terraforged:oak_forest",
                        Pair.of("terraforged:oak_forest", 0.2F),
                        Pair.of("terraforged:oak_large", 0.3F)
                ))
        );
    }

    private static Modifier<Jsonifiable> oakPlains(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:river", "minecraft:plains", "minecraft:sunflower_plains", "minecraft:mountains"),
                FeatureMatcher.and(Blocks.OAK_LOG, Blocks.OAK_LEAVES),
                FeatureReplacer.of(extra(
                        0, 0.02F, 1,
                        "terraforged:oak_forest",
                        Pair.of("terraforged:oak_forest", 0.2F),
                        Pair.of("terraforged:oak_large", 0.3F)
                ))
        );
    }

    private static Modifier<Jsonifiable> flowerForest(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:flower_forest"),
                FeatureMatcher.and(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, Blocks.OAK_LOG, Blocks.OAK_LEAVES),
                FeatureReplacer.of(poisson(
                        15, 0.3F, 500, 0.75F, 2F,
                        "terraforged:birch_forest",
                        Pair.of("terraforged:birch_forest", 0.2F),
                        Pair.of("terraforged:birch_large", 0.2F),
                        Pair.of("terraforged:oak_forest", 0.2F),
                        Pair.of("terraforged:oak_large", 0.2F)
                ))
        );
    }

    private static Modifier<Jsonifiable> birch(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:birch*", "minecraft:tall_birch*"),
                FeatureMatcher.and(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES),
                FeatureReplacer.of(poisson(
                        8, 0.12F, 300, 0.5F, 1.5F,
                        "terraforged:birch_forest",
                        Pair.of("terraforged:birch_forest", 0.2F),
                        Pair.of("terraforged:birch_large", 0.2F)
                ))
        );
    }

    private static Modifier<Jsonifiable> birchOak(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:wooded_hills"),
                FeatureMatcher.and(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, Blocks.OAK_LOG, Blocks.OAK_LEAVES),
                FeatureReplacer.of(poisson(
                        8, 0.35F, 300, 0F, 2F,
                        "terraforged:birch_forest",
                        Pair.of("terraforged:birch_forest", 0.2F),
                        Pair.of("terraforged:birch_large", 0.2F),
                        Pair.of("terraforged:oak_forest", 0.2F),
                        Pair.of("terraforged:oak_large", 0.2F)
                ))
        );
    }

    private static Modifier<Jsonifiable> darkForest(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:dark_forest", "minecraft:dark_forest_hills"),
                FeatureMatcher.and(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES),
                FeatureReplacer.of(poisson(
                        8, 0.35F, 300, 0F, 2F,
                        "terraforged:dark_oak_large",
                        Pair.of("minecraft:huge_brown_mushroom", 0.025F),
                        Pair.of("minecraft:huge_red_mushroom", 0.05F),
                        Pair.of("terraforged:dark_oak_large", 0.3F),
                        Pair.of("terraforged:dark_oak_small", 0.2F),
                        Pair.of("terraforged:birch_forest", 0.05F),
                        Pair.of("terraforged:oak_forest", 0.025F)
                ))
        );
    }

    private static Modifier<Jsonifiable> jungle(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:jungle", "minecraft:modified_jungle", "minecraft:bamboo_jungle"),
                FeatureMatcher.and( "minecraft:tree"),
                FeatureReplacer.of(poisson(
                        6, 0.2F, 400, 0.25F, 2F,
                        "terraforged:jungle_small",
                        Pair.of("terraforged:jungle_small", 0.2F),
                        Pair.of("terraforged:jungle_large", 0.3F),
                        Pair.of("terraforged:jungle_huge", 0.4F),
                        Pair.of("minecraft:jungle_bush", 0.5F)
                ))
        );
    }

    private static Modifier<Jsonifiable> jungleEdge(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:jungle_edge", "minecraft:modified_jungle_edge", "terraforged:stone_forest"),
                FeatureMatcher.and( "minecraft:tree"),
                FeatureReplacer.of(poisson(
                        9, 0.3F, 350, 0.75F, 1.5F,
                        "terraforged:jungle_small",
                        Pair.of("terraforged:jungle_small", 0.2F),
                        Pair.of("terraforged:jungle_large", 0.3F)
                ))
        );
    }

    private static Modifier<Jsonifiable> jungleHills(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:jungle_hills", "minecraft:bamboo_jungle_hills"),
                FeatureMatcher.or( "minecraft:tree"),
                FeatureReplacer.of(poisson(
                        8, 0.2F, 200, 0.35F, 1.85F,
                        "terraforged:jungle_small",
                        Pair.of("terraforged:jungle_small", 0.3F),
                        Pair.of("terraforged:jungle_large", 0.4F),
                        Pair.of("terraforged:jungle_huge", 0.3F),
                        Pair.of("minecraft:jungle_bush", 0.2F)
                ))
        );
    }

    private static Modifier<Jsonifiable> pine(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:taiga", "minecraft:taiga_hills", "minecraft:taiga_mountains"),
                FeatureMatcher.and(Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
                FeatureReplacer.of(poisson(
                        10, 0.25F, 250, 0.0F, 2.75F,
                        "terraforged:pine"
                ))
        );
    }

    private static Modifier<Jsonifiable> redwood(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:giant_spruce_taiga*", "minecraft:giant_tree_taiga*"),
                FeatureMatcher.and("minecraft:giant_trunk_placer", Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
                FeatureReplacer.of(context(
                        new ContextualFeature(template("terraforged:redwood_huge"), 0.4F, Arrays.asList(
                                new Elevation(0.15F, 0F, false),
                                new Biome(0.1F, 0.4F, false)
                        )),
                        new ContextualFeature(template("terraforged:redwood_large"), 0.2F, Arrays.asList(
                                new Elevation(0.25F, 0F, false),
                                new Biome(0.0F, 0.425F, false)
                        )),
                        new ContextualFeature(template("terraforged:spruce_large"), 0.4F, Collections.singletonList(
                                new Elevation(0.35F, 0.15F, false)
                        )),
                        new ContextualFeature(template("terraforged:spruce_small"), 0.2F, Collections.singletonList(
                                new Elevation(0.5F, 0.2F, false)
                        ))
                ).withPlacement(PoissonAtSurface.INSTANCE.configure(new PoissonConfig(
                        8, 0.2F, 250, 0.15F, 2.25F)
                )))
        );
    }

    private static Modifier<Jsonifiable> spruce(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:snowy_taiga", "minecraft:snowy_taiga_hills", "minecraft:taiga_mountains"),
                FeatureMatcher.and("minecraft:tree", Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
                FeatureReplacer.of(poisson(10,0.3F,250,0.15F,1.75F, "terraforged:pine"))
        );
    }

    private static Modifier<Jsonifiable> spruceTundra(GameContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:snowy_tundra", "minecraft:snowy_taiga_mountains", "minecraft:gravelly_mountains", "minecraft:modified_gravelly_mountains"),
                FeatureMatcher.and("minecraft:tree", Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
                FeatureReplacer.of(template("terraforged:pine").withPlacement(Placement.field_242898_b.configure(new ChanceConfig(80))))
        );
    }

    @SafeVarargs
    private static ConfiguredFeature<?, ?> extra(int count, float chance, int extra, String def, Pair<String, Float>... entries) {
        return select(def, entries).withPlacement(Placement.field_242902_f.configure(new AtSurfaceWithExtraConfig(count, chance, extra)));
    }

    @SafeVarargs
    private static ConfiguredFeature<?, ?> poisson(int radius, float fade, int densityScale, float densityMin, float densityMax, String def, Pair<String, Float>... entries) {
        return poisson(new PoissonConfig(radius, fade, densityScale, densityMin, densityMax), def, entries);
    }

    @SafeVarargs
    private static ConfiguredFeature<?, ?> poisson(PoissonConfig config, String def, Pair<String, Float>... entries) {
        return select(def, entries).withPlacement(PoissonAtSurface.INSTANCE.configure(config));
    }

    @SafeVarargs
    private static ConfiguredFeature<?, ?> select(String def, Pair<String, Float>... entries) {
        if (entries.length == 0) {
            return template(def);
        } else if (entries.length == 1) {
            return Feature.RANDOM_BOOLEAN_SELECTOR.withConfiguration(new TwoFeatureChoiceConfig(
                    () -> template(def),
                    () -> template(entries[0].getFirst())
            ));
        } else {
            List<ConfiguredRandomFeatureList> features = new ArrayList<>();
            for (Pair<String, Float> entry : entries) {
                features.add(new ConfiguredRandomFeatureList(
                        getFeature(entry.getFirst()),
                        entry.getSecond()
                ));
            }
            return Feature.RANDOM_SELECTOR.withConfiguration(new MultipleRandomFeatureConfig(features, template(def)));
        }
    }

    private static ConfiguredFeature<?, ?> getFeature(String name) {
        if (name.startsWith("terraforged:")) {
            return template(name);
        }
        return WorldGenRegistries.field_243653_e.func_241873_b(new ResourceLocation(name)).orElseThrow(RuntimeException::new);
    }

    private static ConfiguredFeature<?, ?> brownMushroom() {
        return Features.field_243860_bF;
    }

    private static ConfiguredFeature<?, ?> redMushroom() {
        return Features.field_243861_bG;
    }

    private static ConfiguredFeature<?, ?> context(ContextualFeature... features) {
        return ContextSelectorFeature.INSTANCE.withConfiguration(new ContextSelectorConfig(Arrays.asList(features)));
    }

    private static ConfiguredFeature<?, ?> template(String name) {
        return TerraFeatures.INSTANCE.withConfiguration(TemplateManager.getInstance().getTemplateConfig(new ResourceLocation(name)));
    }
}
