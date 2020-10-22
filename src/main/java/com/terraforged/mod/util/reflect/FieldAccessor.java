package com.terraforged.mod.util.reflect;

import java.lang.reflect.Field;

public class FieldAccessor<O, T> extends Accessor<O, Field> {

    private final Class<T> fieldType;

    FieldAccessor(Class<T> fieldType, Field field) {
        super(field);
        this.fieldType = fieldType;
    }

    public T get(O owner) throws IllegalAccessException {
        Object value = access.get(owner);
        return fieldType.cast(value);
    }

    private static <O, T> FieldAccessor<O, T> byType(Class<O> owner, Class<T> type) {
        for (Field field : owner.getDeclaredFields()) {
            if (field.getType() == type) {
                field.setAccessible(true);
                return new FieldAccessor<>(type, field);
            }
        }
        throw new IllegalStateException("Unable to find field for type " + type + " in class " + owner);
    }
}
