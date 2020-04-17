package com.terraforged.app.util;

import com.google.gson.JsonElement;
import com.terraforged.core.util.serialization.serializer.Reader;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonReader implements Reader {

    private final JsonElement element;

    public JsonReader(JsonElement element) {
        this.element = element;
    }

    @Override
    public int getSize() {
        if (element.isJsonObject()) {
            return element.getAsJsonObject().size();
        }
        if (element.isJsonArray()) {
            return element.getAsJsonArray().size();
        }
        return 0;
    }

    @Override
    public Reader getChild(String key) {
        return new JsonReader(element.getAsJsonObject().get(key));
    }

    @Override
    public Reader getChild(int index) {
        return new JsonReader(element.getAsJsonArray().get(index));
    }

    @Override
    public Collection<String> getKeys() {
        return element.getAsJsonObject().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public String getString() {
        return element.getAsString();
    }

    @Override
    public boolean getBool() {
        return element.getAsBoolean();
    }

    @Override
    public float getFloat() {
        return element.getAsFloat();
    }

    @Override
    public int getInt() {
        return element.getAsInt();
    }
}
