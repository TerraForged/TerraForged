package com.terraforged.mod.biome.utils;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.terraforged.fm.util.codec.Codecs;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class BiomeUtils {

    private static final Codec<Biome.Climate> CLIMATE_CODEC = Biome.Climate.CODEC.codec();
    private static final Map<RegistryKey<Biome>, BiomeBuilder> BUILDERS = new HashMap<>();

    public static BiomeBuilder getBuilder(RegistryKey<Biome> biome) {
        return BUILDERS.computeIfAbsent(biome, BiomeUtils::copy).init();
    }

    public static BiomeBuilder copy(RegistryKey<Biome> key) {
        Biome biome = ForgeRegistries.BIOMES.getValue(key.getLocation());
        if (biome == null) {
            throw new NullPointerException(key.getLocation().toString());
        }

        BiomeBuilder builder = new BiomeBuilder(key, biome);

        builder.scale(biome.getScale());
        builder.depth(biome.getDepth());
        builder.category(biome.getCategory());

        // ambience
        builder.setEffects(biome.getAmbience());

        // climate
        Biome.Climate climate = getClimate(biome);
        builder.downfall(climate.downfall);
        builder.temperature(climate.temperature);
        builder.precipitation(climate.precipitation);
        builder.withTemperatureModifier(climate.temperatureModifier);

        // mobs
        builder.withMobSpawnSettings(biome.getMobSpawnInfo());

        return builder;
    }

    private static Biome.Climate getClimate(Biome biome) {
        JsonElement json = Codecs.encodeAndGet(Biome.CODEC, biome, JsonOps.INSTANCE);
        return Codecs.decodeAndGet(CLIMATE_CODEC, new Dynamic<>(JsonOps.INSTANCE, json));
    }
}
