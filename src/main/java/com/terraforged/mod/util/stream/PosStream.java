package com.terraforged.mod.util.stream;

import net.minecraft.util.math.BlockPos;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PosStream implements FakeStream<BlockPos>, Consumer<BlockPos> {

    private static final Consumer<? super BlockPos> NONE = p -> {};

    private final Consumer<Consumer<? super BlockPos>> source;
    private final ReentrantLock lock = new ReentrantLock();

    private Predicate<? super BlockPos> filter = null;
    private Consumer<? super  BlockPos> consumer = NONE;

    public PosStream(Consumer<Consumer<? super BlockPos>> source) {
        this.source = source;
    }

    @Override
    public Stream<BlockPos> filter(Predicate<? super BlockPos> predicate) {
        if (filter == null) {
            filter = predicate;
        } else {
            filter = new And(filter, predicate);
        }
        return this;
    }

    @Override
    public void forEach(Consumer<? super BlockPos> action) {
        lock.lock();
        consumer = action;
        source.accept(this);
        consumer = NONE;
        lock.unlock();
    }

    @Override
    public void accept(BlockPos pos) {
        if (filter != null && !filter.test(pos)) {
            return;
        }
        consumer.accept(pos);
    }

    private static class And implements Predicate<BlockPos> {

        private final Predicate<? super BlockPos> first;
        private final Predicate<? super BlockPos> second;

        private And(Predicate<? super BlockPos> first, Predicate<? super BlockPos> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean test(BlockPos pos) {
            return first.test(pos) && second.test(pos);
        }
    }
}
