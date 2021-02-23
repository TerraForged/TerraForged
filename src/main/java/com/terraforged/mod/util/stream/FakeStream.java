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

package com.terraforged.mod.util.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

public interface FakeStream<T> extends Stream<T> {

    @Override
    default Stream<T> filter(Predicate<? super T> predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Stream<T> peek(Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Stream<T> limit(long maxSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Stream<T> skip(long n) {
        throw new UnsupportedOperationException();
    }

    @Override
    default IntStream mapToInt(ToIntFunction<? super T> mapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    default LongStream mapToLong(ToLongFunction<? super T> mapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    default DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    default IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    default LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    default DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Stream<T> distinct() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Stream<T> sorted() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Stream<T> sorted(Comparator<? super T> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    default <A> A[] toArray(IntFunction<A[]> generator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default T reduce(T identity, BinaryOperator<T> accumulator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Optional<T> reduce(BinaryOperator<T> accumulator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <R, A> R collect(Collector<? super T, A, R> collector) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Optional<T> min(Comparator<? super T> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Optional<T> max(Comparator<? super T> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean anyMatch(Predicate<? super T> predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean allMatch(Predicate<? super T> predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean noneMatch(Predicate<? super T> predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Optional<T> findFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Optional<T> findAny() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isParallel() {
        return false;
    }

    @Override
    default long count() {
        return 0;
    }

    @Override
    default Stream<T> sequential() {
        return this;
    }

    @Override
    default Stream<T> parallel() {
        return this;
    }

    @Override
    default Stream<T> unordered() {
        return this;
    }

    @Override
    default void forEachOrdered(Consumer<? super T> action) {
        forEach(action);
    }

    @Override
    default Stream<T> onClose(Runnable closeHandler) {
        return this;
    }

    @Override
    default void close() {

    }
}
