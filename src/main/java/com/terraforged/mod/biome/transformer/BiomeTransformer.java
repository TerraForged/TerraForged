/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.biome.transformer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.terraforged.mod.Log;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Function;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BiomeTransformer {

    private static final Codec<BiomeAmbience> EFFECTS = BiomeAmbience.CODEC;
    private static final Codec<Biome.Climate> CLIMATE = Biome.Climate.CODEC.codec();
    private static final Function<String, Biome.Category> CATEGORY = Biome.Category::valueOf;

    @SubscribeEvent
    public static void onLoadBiome(BiomeLoadingEvent event) {
        BiomeTransformerCache cache = BiomeTransformerCache.getInstance();
        JsonElement json = cache.get(event.getName());
        if (json != null && json.isJsonObject()) {
            Log.debug("Transforming biome properties: {}", event.getName());
            JsonObject properties = json.getAsJsonObject();
            applyClimate(event, properties);
            applyCategory(event, properties);
            applyEffects(event, properties);
        }
    }

    private static void applyClimate(BiomeLoadingEvent event, JsonObject properties) {
        Biome.Climate climate = new Biome.Climate(
                getEnum(properties, "precipitation", Biome.RainType.class, event.getClimate().precipitation),
                get(properties, "temperature", JsonElement::getAsFloat, event.getClimate().temperature),
                getEnum(properties, "temperature_modifier", Biome.TemperatureModifier.class, event.getClimate().temperatureModifier),
                get(properties, "downfall", JsonElement::getAsFloat, event.getClimate().downfall)
        );
        event.setClimate(climate);
    }

    private static void applyCategory(BiomeLoadingEvent event, JsonObject properties) {
        Biome.Category category = getEnum(properties, "category", Biome.Category.class, event.getCategory());
        event.setCategory(category);
    }

    private static void applyEffects(BiomeLoadingEvent event, JsonObject properties) {
        BiomeAmbience effects = getCodec(properties, "effects", EFFECTS, event.getEffects());
        event.setEffects(effects);
    }

    private static <T> T get(JsonObject object, String name, Function<JsonElement, T> mapper, T defaultValue) {
        if (object.has(name)) {
            T result = mapper.apply(object.get(name));
            if (result != null) {
                return result;
            }
        }
        return defaultValue;
    }

    private static <T extends Enum<T>> T getEnum(JsonObject properties, String name, Class<T> type, T defaultValue) {
        if (properties.has(name)) {
            String enumName = properties.get(name).getAsString();
            for (T t : type.getEnumConstants()) {
                if (t.name().equalsIgnoreCase(enumName)) {
                    return t;
                }
            }
        }
        return defaultValue;
    }

    private static <T> T getCodec(JsonObject properties, String name, Codec<T> codec, T defaultValue) {
        if (properties.has(name)) {
            try {
                return Codecs.decodeAndGet(codec, properties.get(name), JsonOps.INSTANCE);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return defaultValue;
    }
}
