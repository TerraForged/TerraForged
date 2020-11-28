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
