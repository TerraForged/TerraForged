package com.terraforged.app.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.util.serialization.serializer.Deserializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

public class Loader {

    public static Settings load() {
        Settings settings = new Settings();
        load(settings);
        return settings;
    }

    private static void load(Settings settings) {
        try (Reader reader = new BufferedReader(new FileReader("terraforged-generator.json"))) {
            JsonElement json = new JsonParser().parse(reader);
            new Deserializer().deserialize(new JsonReader(json), settings);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
