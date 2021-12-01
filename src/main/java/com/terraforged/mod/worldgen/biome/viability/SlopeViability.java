package com.terraforged.mod.worldgen.biome.viability;

import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.value.DataValue;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.terrain.TerrainData;

public record SlopeViability(float normalize, float max) implements Viability {
    public static final DataSpec<SlopeViability> SPEC = DataSpec.builder(
                    "Slope",
            SlopeViability.class,
            (data, spec, context) -> new SlopeViability(
                    spec.get("normalize", data, DataValue::asFloat),
                    spec.get("max", data, DataValue::asFloat)))
            .add("normalize", 1F, SlopeViability::normalize)
                    .add("max", 1F, SlopeViability::max)
            .build();

    @Override
    public float getFitness(int x, int z, TerrainData data, Generator generator) {
        float norm = normalize * getScaler(generator);
        float gradient = data.getGradient(x, z, norm);
        return Viability.getFallOff(gradient, max);
    }
}
