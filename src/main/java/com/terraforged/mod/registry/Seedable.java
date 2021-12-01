package com.terraforged.mod.registry;

public interface Seedable<T> {
    T withSeed(long seed);
}
