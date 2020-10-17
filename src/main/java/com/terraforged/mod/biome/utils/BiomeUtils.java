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

    private static final Codec<Biome.Climate> CLIMATE_CODEC = Biome.Climate.field_242459_a.codec();
    private static final Map<RegistryKey<Biome>, BiomeBuilder> BUILDERS = new HashMap<>();

    public static BiomeBuilder getBuilder(RegistryKey<Biome> biome) {
        return BUILDERS.computeIfAbsent(biome, BiomeUtils::copy).init();
    }

    public static BiomeBuilder copy(RegistryKey<Biome> key) {
        Biome biome = ForgeRegistries.BIOMES.getValue(key.func_240901_a_());
        if (biome == null) {
            throw new NullPointerException(key.func_240901_a_().toString());
        }

        BiomeBuilder builder = new BiomeBuilder(key, biome);

        builder.scale(biome.getScale());
        builder.depth(biome.getDepth());
        builder.category(biome.getCategory());

        // ambience
        builder.func_235097_a_(biome.func_235089_q_());

        // climate
        Biome.Climate climate = getClimate(biome);
        builder.downfall(climate.field_242463_e);
        builder.temperature(climate.field_242461_c);
        builder.precipitation(climate.field_242460_b);
        builder.func_242456_a(climate.field_242462_d);

        // mobs
        builder.func_242458_a(biome.func_242433_b());

        return builder;
    }

    private static Biome.Climate getClimate(Biome biome) {
        JsonElement json = Codecs.encodeAndGet(Biome.field_242419_c, biome, JsonOps.INSTANCE);
        return Codecs.decodeAndGet(CLIMATE_CODEC, new Dynamic<>(JsonOps.INSTANCE, json));
    }
}
