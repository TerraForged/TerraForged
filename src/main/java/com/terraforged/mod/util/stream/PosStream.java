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
