/*
 *
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
