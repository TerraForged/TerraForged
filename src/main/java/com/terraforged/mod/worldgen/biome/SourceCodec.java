package com.terraforged.mod.worldgen.biome;

import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.codec.WorldGenCodec;
import net.minecraft.core.RegistryAccess;

public class SourceCodec implements WorldGenCodec<Source> {
    @Override
    public <T> Source decode(DynamicOps<T> ops, T input, RegistryAccess access) {
        return new Source(access);
    }

    @Override
    public <T> T encode(Source source, DynamicOps<T> ops) {
        return ops.empty();
    }
}
