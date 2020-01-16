package com.terraforged.core.util.serialization.serializer;

public interface Writer {

    Writer name(String name);

    Writer beginObject();

    Writer endObject();

    Writer beginArray();

    Writer endArray();

    Writer value(String value);

    Writer value(float value);

    Writer value(int value);

    default void readFrom(Object value) throws IllegalAccessException {
        new Serializer().serialize(value, this);
    }
}
