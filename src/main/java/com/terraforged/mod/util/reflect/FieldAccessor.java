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
