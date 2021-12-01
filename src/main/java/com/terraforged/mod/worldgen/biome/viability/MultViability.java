package com.terraforged.mod.worldgen.biome.viability;

import com.terraforged.cereal.spec.DataSpec;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.terrain.TerrainData;

import java.util.List;

import static com.terraforged.mod.worldgen.biome.viability.SumViability.getRules;

public record MultViability(Viability[] rules) implements Viability {
    public static final DataSpec<MultViability> SPEC = DataSpec.builder(
            "Multiply",
            MultViability.class,
            (data, spec, context) -> new MultViability(spec.get("rules", data, v -> getRules(v, context))))
            .addList("rules", MultViability::getRulesList).build();

    @Override
    public float getFitness(int x, int z, TerrainData data, Generator generator) {
        float value = 1F;

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < rules.length; i++) {
            value *= rules[i].getFitness(x, z, data, generator);
        }

        return value;
    }

    private List<Viability> getRulesList() {
        return List.of(rules);
    }
}
