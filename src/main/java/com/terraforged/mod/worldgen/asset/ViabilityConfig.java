package com.terraforged.mod.worldgen.asset;

import com.mojang.serialization.Codec;
import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.spec.DataSpecs;
import com.terraforged.mod.codec.SpecCodec;
import com.terraforged.mod.registry.DataSeedable;
import com.terraforged.mod.util.DataUtil;
import com.terraforged.mod.worldgen.biome.viability.*;
import net.minecraft.world.level.biome.Biome;

public record ViabilityConfig(Biome.BiomeCategory category, Viability viability) implements DataSeedable<ViabilityConfig> {
    public static final ViabilityConfig NONE = new ViabilityConfig(Biome.BiomeCategory.NONE, Viability.NONE);

    public static final DataSpec<ViabilityConfig> SPEC = DataSpec.builder(
            ViabilityConfig.class,
            (data, spec, context) -> new ViabilityConfig(
                    spec.getEnum("category", data, Biome.BiomeCategory.class),
                    spec.get("viability", data, Viability.class, context)))
            .add("category", Biome.BiomeCategory.NONE, ViabilityConfig::category)
            .addObj("viability", ViabilityConfig::viability)
            .build();

    public static final Codec<ViabilityConfig> CODEC = SpecCodec.of(SPEC);

    @Override
    public DataSpec<ViabilityConfig> getSpec() {
        return SPEC;
    }

    static {
        DataSpecs.register(SPEC);
        DataUtil.registerSub(Viability.class, MultViability.SPEC);
        DataUtil.registerSub(Viability.class, HeightViability.SPEC);
        DataUtil.registerSub(Viability.class, NoiseViability.SPEC);
        DataUtil.registerSub(Viability.class, SlopeViability.SPEC);
        DataUtil.registerSub(Viability.class, SumViability.SPEC);
    }
}
