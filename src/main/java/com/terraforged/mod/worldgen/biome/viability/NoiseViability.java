package com.terraforged.mod.worldgen.biome.viability;

import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.noise.Module;

public record NoiseViability(Module noise) implements Viability {
    public static final DataSpec<NoiseViability> SPEC = DataSpec.builder(
                    "Noise",
            NoiseViability.class,
            (data, spec, context) -> new NoiseViability(spec.get("noise", data, Module.class, context)))
            .addObj("noise", NoiseViability::noise)
            .build();

    @Override
    public float getFitness(int x, int z, TerrainData data, Generator generator) {
        return noise.getValue(x, z);
    }
}
