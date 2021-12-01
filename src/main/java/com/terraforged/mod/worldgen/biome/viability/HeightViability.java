package com.terraforged.mod.worldgen.biome.viability;

import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.cereal.value.DataValue;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.terrain.TerrainData;

public record HeightViability(float minOffset, float midOffset, float maxOffset) implements Viability {
    public static final DataSpec<HeightViability> SPEC = DataSpec.builder(
                    "Height",
                    HeightViability.class,
                    (data, spec, context) -> new HeightViability(
                            spec.get("min", data, DataValue::asFloat),
                            spec.get("mid", data, DataValue::asFloat),
                            spec.get("max", data, DataValue::asFloat)
                    )
            )
            .add("min", 0.0F, HeightViability::minOffset)
            .add("mid", 0.5F, HeightViability::midOffset)
            .add("max", 1.0F, HeightViability::maxOffset)
            .build();

    @Override
    public float getFitness(int x, int z, TerrainData data, Generator generator) {
        int height = data.getHeight(x, z);

        float scale = getScaler(generator);
        float min = generator.getSeaLevel() + minOffset() * scale;
        float mid = generator.getSeaLevel() + midOffset() * scale;
        float max = generator.getSeaLevel() + maxOffset() * scale;

        return Viability.getFallOff(height, min, mid, max);
    }
}
