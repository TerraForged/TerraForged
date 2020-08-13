package com.terraforged.mod.util.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
