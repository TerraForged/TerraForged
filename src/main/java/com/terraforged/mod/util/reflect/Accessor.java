package com.terraforged.mod.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class Accessor<O, A> {

    protected final A access;

    public Accessor(A access) {
        this.access = access;
    }

    public static <O, T> FieldAccessor<O, T> field(Class<O> owner, Class<T> fieldType) {
        return field(owner, fieldType, f -> true);
    }

    public static <O, T> FieldAccessor<O, T> field(Class<O> owner, Class<T> fieldType, Predicate<Field> predicate) {
        for (Field field : owner.getDeclaredFields()) {
            if (field.getType() == fieldType && predicate.test(field)) {
                field.setAccessible(true);
                return new FieldAccessor<>(fieldType, field);
            }
        }
        throw new IllegalStateException("Unable to find matching field " + " in class " + owner);
    }

    public static <O, T> MethodAccessor<O> method(Class<O> owner, Predicate<Method> predicate) {
        for (Method method : owner.getDeclaredMethods()) {
            if (predicate.test(method)) {
                method.setAccessible(true);
                return new MethodAccessor<>(method);
            }
        }
        throw new IllegalStateException("Unable to find matching method " + " in class " + owner);
    }

    public static Predicate<Method> paramTypes(Class<?>... paramTypes) {
        return method -> {
            Class<?>[] types = method.getParameterTypes();
            if (types.length != paramTypes.length) {
                return false;
            }
            for (int i = 0; i < types.length; i++) {
                if (types[i] != paramTypes[i]) {
                    return false;
                }
            }
            return true;
        };
    }

    public static Predicate<Field> fieldModifiers(IntPredicate... flags) {
        return modifiers(flags);
    }

    public static Predicate<Method> methodModifiers(IntPredicate... flags) {
        return modifiers(flags);
    }

    public static <T extends Member> Predicate<T> modifiers(IntPredicate... flags) {
        return t -> {
            int modifiers = t.getModifiers();
            for (IntPredicate flag : flags) {
                if (!flag.test(modifiers)) {
                    return false;
                }
            }
            return true;
        };
    }
}
