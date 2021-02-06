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
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.data.gen.FeatureInjectorProvider;
import com.terraforged.mod.feature.TerraFeatures;
import com.terraforged.mod.feature.context.ContextSelectorConfig;
import com.terraforged.mod.feature.context.ContextSelectorFeature;
import com.terraforged.mod.feature.context.ContextualFeature;
import com.terraforged.mod.feature.context.modifier.BiomeModifier;
import com.terraforged.mod.feature.context.modifier.ContextModifier;
import com.terraforged.mod.feature.context.modifier.Elevation;
import com.terraforged.mod.feature.decorator.FilterDecorator;
import com.terraforged.mod.feature.decorator.fastpoisson.FastPoissonAtSurface;
import com.terraforged.mod.feature.decorator.fastpoisson.FastPoissonConfig;
import com.terraforged.mod.feature.decorator.filter.FilterDecoratorConfig;
import com.terraforged.mod.feature.decorator.filter.PlacementFilter;
import com.terraforged.mod.featuremanager.matcher.biome.BiomeMatcher;
import com.terraforged.mod.featuremanager.matcher.feature.FeatureMatcher;
import com.terraforged.mod.featuremanager.modifier.Jsonifiable;
import com.terraforged.mod.featuremanager.modifier.Modifier;
import com.terraforged.mod.featuremanager.template.template.TemplateManager;
import com.terraforged.mod.featuremanager.transformer.FeatureReplacer;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.Placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Trees {

    public static void addInjectors(FeatureInjectorProvider provider) {
        provider.add("trees/acacia", acacia(provider.getContext()));
        provider.add("trees/oak_badlands", oakBadlands(provider.getContext()));
        provider.add("trees/oak_wooded_badlands", oakWoodedBadlands(provider.getContext()));
        provider.add("trees/oak_forest", oakForest(provider.getContext()));
        provider.add("trees/oak_plains", oakPlains(provider.getContext()));
        provider.add("trees/birch", birch(provider.getContext()));
        provider.add("trees/birch_oak", birchOak(provider.getContext()));
        provider.add("trees/dark_oak", darkForest(provider.getContext()));
        provider.add("trees/fir_forest", firForest(provider.getContext()));
        provider.add("trees/flower_plains", flowerForest(provider.getContext()));
        provider.add("trees/jungle", jungle(provider.getContext()));
        provider.add("trees/jungle_edge", jungleEdge(provider.getContext()));
        provider.add("trees/jungle_hills", jungleHills(provider.getContext()));
        provider.add("trees/pine", pine(provider.getContext()));
        provider.add("trees/redwood", redwood(provider.getContext()));
        provider.add("trees/spruce", spruce(provider.getContext()));
        provider.add("trees/spruce_tundra", spruceTundra(provider.getContext()));
        provider.add("trees/willow", willow(provider.getContext()));
    }

    private static Modifier<Jsonifiable> acacia(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, Biome.Category.SAVANNA),
                FeatureMatcher.and(Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES),
                FeatureReplacer.of(extra(
                        0, 0.225F, 1,
                        "terraforged:acacia_large",
                        Pair.of("terraforged:acacia_large", 0.4F),
                        Pair.of("terraforged:acacia_small", 0.15F)
                ))
        );
    }

    private static Modifier<Jsonifiable> oakBadlands(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(
                        context,
                        "minecraft:shattered_savanna*",
                        "minecraft:modified_badlands*"
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

    private static Modifier<Jsonifiable> oakWoodedBadlands(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(
                        context,
                        "minecraft:wooded_badlands*",
                        "minecraft:modified_wooded*"
                ),
                FeatureMatcher.and(Blocks.OAK_LOG, Blocks.OAK_LEAVES),
                FeatureReplacer.of(poisson(
                        0.2F, 0.8F, 8, 0.25F, 150, 0.75F,
                        "terraforged:oak_small",
                        Pair.of("terraforged:oak_small", 0.3F),
                        Pair.of("terraforged:acacia_bush", 0.2F)
                ))
        );
    }

    private static Modifier<Jsonifiable> oakForest(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:forest"),
                FeatureMatcher.and(Blocks.OAK_LOG, Blocks.OAK_LEAVES),
                FeatureReplacer.of(poisson(
                        0.25F, 7, 0.3F, 150, 0.75F,
                        "terraforged:oak_forest",
                        Pair.of("terraforged:oak_forest", 0.2F),
                        Pair.of("terraforged:oak_large", 0.3F)
                ))
        );
    }

    private static Modifier<Jsonifiable> oakPlains(TFBiomeContext context) {
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

    private static Modifier<Jsonifiable> firForest(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:snowy_mountains", "minecraft:wooded_mountains", "terraforged:fir_forest", "terraforged:snowy_fir_forest"),
                FeatureMatcher.and(Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
                FeatureReplacer.of(context(
                        contextEntry("terraforged:spruce_small", 0.1F, eCntx(0.55F, 0.2F)),
                        contextEntry("terraforged:spruce_large", 0.25F, eCntx(0.3F, 0F))
                ).withPlacement(poisson(0.3F, 4, 0.3F, 300, 0.5F)))
        );
    }

    private static Modifier<Jsonifiable> flowerForest(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, Biomes.FLOWER_FOREST, ModBiomes.FLOWER_PLAINS),
                FeatureMatcher.and(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, Blocks.OAK_LOG, Blocks.OAK_LEAVES),
                FeatureReplacer.of(poisson(
                        0.2F, 8, 0.1F, 500, 0.75F,
                        "terraforged:birch_forest",
                        Pair.of("terraforged:birch_forest", 0.2F),
                        Pair.of("terraforged:birch_large", 0.2F),
                        Pair.of("terraforged:oak_forest", 0.2F),
                        Pair.of("terraforged:oak_large", 0.2F)
                ))
        );
    }

    private static Modifier<Jsonifiable> birch(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:birch*", "minecraft:tall_birch*"),
                FeatureMatcher.and(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES),
                FeatureReplacer.of(context(
                        contextEntry("terraforged:birch_large", 0.2F, eCntx(0.25F, 0F), bCntx(0.1F, 0.3F)),
                        contextEntry("terraforged:birch_forest", 0.2F, eCntx(0.3F, 0F), bCntx(0.05F, 0.2F)),
                        contextEntry("terraforged:birch_small", 0.1F, bCntx(0.25F, 0F)),
                        contextEntry("terraforged:birch_small", 0.1F, eCntx(0.25F, 0.65F))
                ).withPlacement(poisson(0.4F, 7, 0.25F, 175, 0.95F)))
        );
    }

    private static Modifier<Jsonifiable> birchOak(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:wooded_hills"),
                FeatureMatcher.and(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, Blocks.OAK_LOG, Blocks.OAK_LEAVES),
                FeatureReplacer.of(poisson(
                        0.25F, 4, 0.25F, 300, 0.75F,
                        "terraforged:birch_forest",
                        Pair.of("terraforged:birch_forest", 0.2F),
                        Pair.of("terraforged:birch_large", 0.2F),
                        Pair.of("terraforged:oak_forest", 0.2F),
                        Pair.of("terraforged:oak_large", 0.2F)
                ))
        );
    }

    private static Modifier<Jsonifiable> darkForest(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:dark_forest", "minecraft:dark_forest_hills"),
                FeatureMatcher.and(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES),
                FeatureReplacer.of(poisson(
                        0.3F, 5, 0.2F, 300, 0.75F,
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

    private static Modifier<Jsonifiable> jungle(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:jungle", "minecraft:modified_jungle", "minecraft:bamboo_jungle"),
                FeatureMatcher.and("minecraft:tree"),
                FeatureReplacer.of(poisson(
                        0.4F, 6, 0.2F, 400, 0.75F,
                        "terraforged:jungle_small",
                        Pair.of("terraforged:jungle_small", 0.2F),
                        Pair.of("terraforged:jungle_large", 0.3F),
                        Pair.of("terraforged:jungle_huge", 0.4F),
                        Pair.of("minecraft:jungle_bush", 0.5F)
                ))
        );
    }

    private static Modifier<Jsonifiable> jungleEdge(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:jungle_edge", "minecraft:modified_jungle_edge", "terraforged:stone_forest"),
                FeatureMatcher.and("minecraft:tree"),
                FeatureReplacer.of(poisson(
                        0.35F, 8, 0.25F, 350, 0.75F,
                        "terraforged:jungle_small",
                        Pair.of("terraforged:jungle_small", 0.2F),
                        Pair.of("terraforged:jungle_large", 0.3F),
                        Pair.of("minecraft:jungle_bush", 0.4F)
                ))
        );
    }

    private static Modifier<Jsonifiable> jungleHills(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:jungle_hills", "minecraft:bamboo_jungle_hills"),
                FeatureMatcher.or("minecraft:tree"),
                FeatureReplacer.of(poisson(
                        0.35F, 7, 0.25F, 200, 0.75F,
                        "terraforged:jungle_small",
                        Pair.of("terraforged:jungle_small", 0.3F),
                        Pair.of("terraforged:jungle_large", 0.4F),
                        Pair.of("terraforged:jungle_huge", 0.3F),
                        Pair.of("minecraft:jungle_bush", 0.2F)
                ))
        );
    }

    private static Modifier<Jsonifiable> pine(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:taiga", "minecraft:taiga_hills", "minecraft:taiga_mountains"),
                FeatureMatcher.and(Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
                FeatureReplacer.of(poisson(
                        0.25F, 7, 0.25F, 250, 0.7F,
                        "terraforged:pine"
                ))
        );
    }

    private static Modifier<Jsonifiable> redwood(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:giant_spruce_taiga*", "minecraft:giant_tree_taiga*"),
                FeatureMatcher.and("minecraft:giant_trunk_placer", Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
                FeatureReplacer.of(context(
                        contextEntry("terraforged:redwood_huge", 0.4F, eCntx(0.15F, 0F), bCntx(0.1F, 0.3F)),
                        contextEntry("terraforged:redwood_large", 0.2F, eCntx(0.25F, 0F), bCntx(0.05F, 0.25F)),
                        contextEntry("terraforged:spruce_large", 0.4F, eCntx(0.35F, 0.15F)),
                        contextEntry("terraforged:spruce_small", 0.2F, eCntx(0.5F, 0.2F))
                ).withPlacement(poisson(0.3F, 6, 0.3F, 250, 0.75F)))
        );
    }

    private static Modifier<Jsonifiable> spruce(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:snowy_taiga", "minecraft:snowy_taiga_hills", "minecraft:taiga_mountains"),
                FeatureMatcher.and("minecraft:tree", Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
                FeatureReplacer.of(poisson(0.3F, 7, 0.25F, 250, 0.75F, "terraforged:pine"))
        );
    }

    private static Modifier<Jsonifiable> spruceTundra(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, "minecraft:snowy_tundra", "minecraft:snowy_taiga_mountains", "minecraft:gravelly_mountains", "minecraft:modified_gravelly_mountains"),
                FeatureMatcher.and("minecraft:tree", Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES),
                FeatureReplacer.of(template("terraforged:pine").withPlacement(Placement.CHANCE.configure(new ChanceConfig(80))))
        );
    }

    private static Modifier<Jsonifiable> willow(TFBiomeContext context) {
        return new Modifier<>(
                BiomeMatcher.of(context, Biomes.SWAMP, Biomes.SWAMP_HILLS),
                FeatureMatcher.and("minecraft:tree", Blocks.OAK_LEAVES, Blocks.OAK_LOG),
                FeatureReplacer.of(extra(7, 0.1F, 1,
                        "terraforged:willow_large",
                        Pair.of("terraforged:willow_small", 0.2F),
                        Pair.of("terraforged:willow_large", 0.35F)
                ))
        );
    }

    @SafeVarargs
    private static ConfiguredFeature<?, ?> extra(int count, float chance, int extra, String def, Pair<String, Float>... entries) {
        return select(def, entries)
                .withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT)
                .withPlacement(Placement.COUNT_EXTRA.configure(new AtSurfaceWithExtraConfig(count, chance, extra)));
    }

    @SafeVarargs
    private static ConfiguredFeature<?, ?> poisson(float scale, int radius, float fade, int densityScale, float densityVariation, String def, Pair<String, Float>... entries) {
        return poisson(scale, FastPoissonConfig.LEGACY_JITTER, radius, fade, densityScale, densityVariation, def, entries);
    }

    @SafeVarargs
    private static ConfiguredFeature<?, ?> poisson(float scale, float jitter, int radius, float fade, int densityScale, float densityVariation, String def, Pair<String, Float>... entries) {
        return poisson(new FastPoissonConfig(scale, jitter, radius, fade, densityScale, densityVariation), def, entries);
    }

    @SafeVarargs
    private static ConfiguredFeature<?, ?> poisson(FastPoissonConfig config, String def, Pair<String, Float>... entries) {
        return select(def, entries).withPlacement(poisson(config));
    }

    private static ConfiguredPlacement<?> poisson(float scale, int radius, float fade, int densityScale, float densityVariation) {
        return poisson(new FastPoissonConfig(scale, radius, fade, densityScale, densityVariation));
    }

    private static ConfiguredPlacement<?> poisson(FastPoissonConfig config) {
        ConfiguredPlacement<?> poisson = FastPoissonAtSurface.INSTANCE.configure(config);
        Optional<ConfiguredPlacement<?>> filtered = PlacementFilter.decode("biome")
                .map(f -> FilterDecorator.INSTANCE.configure(new FilterDecoratorConfig(poisson, f)));
        if (filtered.isPresent()) {
            return filtered.get();
        }
        return poisson;
    }

    private static ContextModifier bCntx(float from, float to) {
        return bCntx(from, to, false);
    }

    private static ContextModifier bCntx(float from, float to, boolean exclusive) {
        return new BiomeModifier(from, to, exclusive);
    }

    private static ContextModifier eCntx(float from, float to) {
        return eCntx(from, to, false);
    }

    private static ContextModifier eCntx(float from, float to, boolean exclusive) {
        return new Elevation(from, to, exclusive);
    }

    private static ContextualFeature contextEntry(String template, float chance, ContextModifier... contexts) {
        return new ContextualFeature(template(template), chance, Arrays.asList(contexts));
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
        return WorldGenRegistries.CONFIGURED_FEATURE.getOptional(new ResourceLocation(name)).orElseThrow(RuntimeException::new);
    }

    private static ConfiguredFeature<?, ?> context(ContextualFeature... features) {
        return ContextSelectorFeature.INSTANCE.withConfiguration(new ContextSelectorConfig(Arrays.asList(features)));
    }

    private static ConfiguredFeature<?, ?> template(String name) {
        return TerraFeatures.INSTANCE.withConfiguration(TemplateManager.getInstance().getTemplateConfig(new ResourceLocation(name)));
    }
}
