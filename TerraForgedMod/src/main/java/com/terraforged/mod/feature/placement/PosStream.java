package com.terraforged.mod.feature.placement;

import net.minecraft.util.math.BlockPos;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class PosStream {

    private static final ThreadLocal<BlockPos.Mutable> MUTABLE_THREAD_LOCAL = ThreadLocal.withInitial(BlockPos.Mutable::new);

    private PosStream() {

    }

    public static Stream<BlockPos> of(int count, Populator populator) {
        return StreamSupport.stream(new PosSpliterator(count, populator), false);
    }

    @FunctionalInterface
    public interface Populator {

        /**
         * Populates the BlockPos.Mutable with the next x,y,z coordinates
         *
         * @param next the BlockPos.Mutable to populate
         * @return true if the populated BlockPos.Mutable should be used next in the stream
         */
        boolean populate(BlockPos.Mutable next);
    }

    private static class PosSpliterator implements Spliterator<BlockPos> {

        private final int attempts;
        private final Populator populator;
        private final BlockPos.Mutable pos = MUTABLE_THREAD_LOCAL.get();

        private int counter = -1;

        private PosSpliterator(int attempts, Populator populator) {
            this.attempts = attempts;
            this.populator = populator;
        }

        @Override
        public boolean tryAdvance(Consumer<? super BlockPos> action) {
            if (counter < attempts) {
                counter++;
                if (populator.populate(pos)) {
                    action.accept(pos);
                }
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<BlockPos> trySplit() {
            // cannot split
            return null;
        }

        @Override
        public long estimateSize() {
            return attempts;
        }

        @Override
        public int characteristics() {
            // assume that positions are generated from a seeded random .: ordered
            return Spliterator.ORDERED;
        }
    }
}
