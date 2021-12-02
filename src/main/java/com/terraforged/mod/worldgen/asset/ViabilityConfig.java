package com.terraforged.mod.worldgen.asset;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.cereal.Cereal;
import com.terraforged.cereal.spec.Context;
import com.terraforged.mod.registry.Seedable;
import com.terraforged.mod.util.DataUtil;
import com.terraforged.mod.worldgen.biome.viability.*;

import java.util.function.Supplier;

public record ViabilityConfig(Supplier<BiomeTag> biomes, float density, Viability viability) implements Seedable<ViabilityConfig> {
    public static final ViabilityConfig NONE = new ViabilityConfig(Suppliers.ofInstance(BiomeTag.NONE), 1F, Viability.NONE);

    public static final Codec<ViabilityConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeTag.REFERENCE_CODEC.fieldOf("biomes").forGetter(ViabilityConfig::biomes),
            Codec.FLOAT.optionalFieldOf("density", 1F).forGetter(ViabilityConfig::density),
            ViabilityCodec.CODEC.fieldOf("viability").forGetter(ViabilityConfig::viability)
    ).apply(instance, ViabilityConfig::new));

    @Override
    public ViabilityConfig withSeed(long seed) {
        var data = Cereal.serialize(viability);
        var context = new Context();
        context.getData().add("seed", seed);
        var viability = Cereal.deserialize(data.asObj(), Viability.class, context);
        return new ViabilityConfig(biomes, density, viability);
    }

    static {
        DataUtil.registerSub(Viability.class, MultViability.SPEC);
        DataUtil.registerSub(Viability.class, HeightViability.SPEC);
        DataUtil.registerSub(Viability.class, NoiseViability.SPEC);
        DataUtil.registerSub(Viability.class, SlopeViability.SPEC);
        DataUtil.registerSub(Viability.class, SumViability.SPEC);
    }
}
