package com.terraforged.mod.registry;

import com.terraforged.cereal.spec.Context;
import com.terraforged.cereal.spec.DataSpec;

public interface DataSeedable<T> extends Seedable<T> {
    DataSpec<T> getSpec();

    @Override
    default T withSeed(long seed) {
        try {
            var data = getSpec().serialize(this);

            var context = new Context();
            context.getData().add("seed", seed);

            return getSpec().deserialize(data.asObj(), context);
        } catch (Throwable t) {
            throw new Error("Failed to reseed object: " + this, t);
        }
    }
}
