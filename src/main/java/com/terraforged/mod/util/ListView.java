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

package com.terraforged.mod.util;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ListView<T> implements Iterable<T> {

    private final Iterable<T> collection;
    private final Predicate<T> filter;

    public ListView(Iterable<T> collection, Predicate<T> filter) {
        this.collection = collection;
        this.filter = filter;
    }

    public void iterate(Consumer<T> consumer) {
        for (T t : collection) {
            if (filter.test(t)) {
                continue;
            }
            consumer.accept(t);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new FilterIterator();
    }

    private class FilterIterator implements Iterator<T> {

        private final Iterator<T> iterator = collection.iterator();
        private T next = null;

        @Override
        public boolean hasNext() {
            if (iterator.hasNext()) {
                this.next = iterator.next();

                if (filter.test(next)) {
                    return hasNext();
                }

                return true;
            }
            return false;
        }

        @Override
        public T next() {
            return next;
        }
    }
}
