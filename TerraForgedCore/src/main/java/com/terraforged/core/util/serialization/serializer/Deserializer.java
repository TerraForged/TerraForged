package com.terraforged.core.util.serialization.serializer;

import com.terraforged.core.util.serialization.annotation.Serializable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class Deserializer {

    public void deserialize(Reader reader, Object object) throws Throwable {
        Class<?> type = object.getClass();
        for (String name : reader.getKeys()) {
            if (name.charAt(0) == '#') {
                continue;
            }

            Reader child = reader.getChild(name);
            Field field = type.getField(name);
            if (Serializer.isSerializable(field)) {
                field.setAccessible(true);
                fromValue(child, object, field);
            }
        }
    }

    private void fromValue(Reader reader, Object object, Field field) throws Throwable {
        if (field.getType() == int.class) {
            field.set(object, reader.getInt("value"));
            return;
        }
        if (field.getType() == float.class) {
            field.set(object, reader.getFloat("value"));
            return;
        }
        if (field.getType() == boolean.class) {
            field.set(object, reader.getString("value").equals("true"));
            return;
        }
        if (field.getType() == String.class) {
            field.set(object, reader.getString("value"));
            return;
        }
        if (field.getType().isEnum()) {
            String name = reader.getString("value");
            for (Enum<?> e : field.getType().asSubclass(Enum.class).getEnumConstants()) {
                if (e.name().equals(name)) {
                    field.set(object, e);
                    return;
                }
            }
        }
        if (field.getType().isAnnotationPresent(Serializable.class)) {
            Reader child = reader.getChild("value");
            Object value = field.getType().newInstance();
            deserialize(child, value);
            field.set(object, value);
            return;
        }
        if (field.getType().isArray()) {
            Class<?> type = field.getType().getComponentType();
            if (type.isAnnotationPresent(Serializable.class)) {
                Reader child = reader.getChild("value");
                Object array = Array.newInstance(type, child.getSize());
                for (int i = 0; i < child.getSize(); i++) {
                    Object value = type.newInstance();
                    deserialize(child.getChild(i), value);
                    Array.set(array, i, value);
                }
                field.set(object, array);
            }
        }
    }

    private static String getName(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                c = Character.toLowerCase(c);
            } else if (c == ' ' && i + 1 < name.length()) {
                c = Character.toUpperCase(name.charAt(++i));
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
