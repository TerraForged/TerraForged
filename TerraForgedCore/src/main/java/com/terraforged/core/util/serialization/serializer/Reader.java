package com.terraforged.core.util.serialization.serializer;

import java.util.Collection;

public interface Reader {

    int getSize();

    Reader getChild(String key);

    Reader getChild(int index);

    Collection<String> getKeys();

    String getString();

    boolean getBool();

    float getFloat();

    int getInt();

    default String getString(String key) {
        return getChild(key).getString();
    }

    default boolean getBool(String key) {
        return getChild(key).getBool();
    }

    default float getFloat(String key) {
        return getChild(key).getFloat();
    }

    default int getInt(String key) {
        return getChild(key).getInt();
    }

    default String getString(int index) {
        return getChild(index).getString();
    }

    default boolean getBool(int index) {
        return getChild(index).getBool();
    }

    default float getFloat(int index) {
        return getChild(index).getFloat();
    }

    default int getInt(int index) {
        return getChild(index).getInt();
    }

    default void writeTo(Object object) throws Throwable {
        new Deserializer().deserialize(this, object);
    }
}
