package com.terraforged.core.util.serialization.serializer;

import com.terraforged.core.util.serialization.annotation.Comment;
import com.terraforged.core.util.serialization.annotation.Range;
import com.terraforged.core.util.serialization.annotation.Serializable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Serializer {

    public void serialize(Object object, Writer writer) throws IllegalAccessException {
        if (object.getClass().isArray()) {
            writer.beginArray();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(object, i);
                serialize(element, writer);
            }
            writer.endArray();
        } else if (!object.getClass().isPrimitive()) {
            int order = 0;
            writer.beginObject();
            for (Field field : object.getClass().getFields()) {
                if (Serializer.isSerializable(field)) {
                    field.setAccessible(true);
                    write(object, field, order, writer);
                    order++;
                }
            }
            writer.endObject();
        }
    }

    private void write(Object object, Field field, int order, Writer writer) throws IllegalAccessException {
        if (field.getType() == int.class) {
            writer.name(getName(field));
            writer.beginObject();
            writer.name("value").value((int) field.get(object));
            writeMeta(field, order, writer);
            writer.endObject();
            return;
        }
        if (field.getType() == float.class) {
            writer.name(getName(field));
            writer.beginObject();
            writer.name("value").value((float) field.get(object));
            writeMeta(field, order, writer);
            writer.endObject();
            return;
        }
        if (field.getType() == String.class) {
            writer.name(getName(field));
            writer.beginObject();
            writer.name("value").value((String) field.get(object));
            writeMeta(field, order, writer);
            writer.endObject();
            return;
        }
        if (field.getType().isEnum()) {
            writer.name(getName(field));
            writer.beginObject();
            writer.name("value").value(((Enum) field.get(object)).name());
            writeMeta(field, order, writer);
            writer.endObject();
            return;
        }
        if (field.getType().isArray()) {
            if (field.getType().getComponentType().isAnnotationPresent(Serializable.class)) {
                writer.name(getName(field));
                writer.beginObject();
                writer.name("value");
                serialize(field.get(object), writer);
                writeMeta(field, order, writer);
                writer.endObject();
            }
            return;
        }
        if (field.getType().isAnnotationPresent(Serializable.class)) {
            writer.name(getName(field));
            writer.beginObject();
            writer.name("value");
            serialize(field.get(object), writer);
            writeMeta(field, order, writer);
            writer.endObject();
        }
    }

    private void writeMeta(Field field, int order, Writer writer) {
        writer.name("_name").value(getName(field));
        writer.name("_order").value(order);

        Range range = field.getAnnotation(Range.class);
        if (range != null) {
            if (field.getType() == int.class) {
                writer.name("_min").value((int) range.min());
                writer.name("_max").value((int) range.max());
            } else {
                writer.name("_min").value(range.min());
                writer.name("_max").value(range.max());
            }
        }

        Comment comment = field.getAnnotation(Comment.class);
        if (comment != null) {
            writer.name("_comment");
            writer.beginArray();
            for (String line : comment.value()) {
                writer.value(line);
            }
            writer.endArray();
        }

        if (field.getType().isEnum()) {
            writer.name("_options");
            writer.beginArray();
            for (Enum o : field.getType().asSubclass(Enum.class).getEnumConstants()) {
                writer.value(o.name());
            }
            writer.endArray();
        }
    }

    private static String getName(Field field) {
        String name = field.getName();
        StringBuilder sb = new StringBuilder(name.length() * 2);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    protected static boolean isSerializable(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers)
                && !Modifier.isFinal(modifiers)
                && !Modifier.isStatic(modifiers)
                && !Modifier.isTransient(modifiers);
    }
}
