/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.util.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class ReflectUtils {

    public static MethodHandle field(Class<?> owner, Class<?> type, String... names) {
        try {
            Field field = getField(owner, type, f -> contains(names, f.getName()));
            return MethodHandles.lookup().in(owner).unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle method(Class<?> owner, Class<?> type, String... names) {
        try {
            Method method = getMethod(owner, type, f -> contains(names, f.getName()));
            return MethodHandles.lookup().in(owner).unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle method(Class<?> owner, Class<?> type, Class<?>... argTypes) {
        try {
            Method method = getMethod(owner, type, m -> Arrays.equals(argTypes, m.getParameterTypes()));
            return MethodHandles.lookup().in(owner).unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle method(Class<?> owner, Class<?> type, String[] names, Class<?>[] argTypes) {
        try {
            Method method = getMethod(owner, type, m -> contains(names, m.getName()) && equals(argTypes, m.getParameterTypes()));
            return MethodHandles.lookup().in(owner).unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getField(Class<?> owner, Class<?> fieldType, Predicate<Field> predicate) {
        return accessMember(owner, fieldType, owner.getDeclaredFields(), Field::getType, predicate);
    }

    public static Method getMethod(Class<?> owner, Class<?> fieldType, Predicate<Method> predicate) {
        return accessMember(owner, fieldType, owner.getDeclaredMethods(), Method::getReturnType, predicate);
    }

    public static <T extends AccessibleObject & Member> T accessMember(
            Class<?> owner,
            Class<?> type,
            T[] members,
            Function<T, Class<?>> typeGetter,
            Predicate<T> predicate) {

        for (T member : members) {
            if (typeGetter.apply(member) == type && predicate.test(member)) {
                member.setAccessible(true);
                return member;
            }
        }

        throw new IllegalStateException("Unable to find matching member in class " + owner);
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

    private static <T> boolean contains(T[] array, T value) {
        if (array.length > 0) {
            for (T t : array) {
                if (t.equals(value)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @SafeVarargs
    private static <T> boolean equals(T[] array, T... value) {
        return Arrays.equals(array, value);
    }
}
