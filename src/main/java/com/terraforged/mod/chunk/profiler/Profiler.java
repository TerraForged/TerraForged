package com.terraforged.mod.chunk.profiler;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public enum Profiler {
    FIND_STRUCTURE,
    STRUCTURE_START,
    STRUCTURE_SPREAD,
    BIOMES,
    TERRAIN,
    SURFACE,
    CARVING,
    DECORATION,
    MOB_SPAWNS,
    ;

    private final AtomicLong time = new AtomicLong();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong longest = new AtomicLong();
    private final AtomicLong shortest = new AtomicLong(Long.MAX_VALUE);
    private final ThreadLocal<Section> section = ThreadLocal.withInitial(ProfilerSection::new);

    public Section punchIn() {
        return section.get().punchIn();
    }

    public long timeMS() {
        return TimeUnit.NANOSECONDS.toMillis(time.get());
    }

    public long minMS() {
        long min = shortest.get();
        if (min == Long.MAX_VALUE) {
            return 0;
        }
        return TimeUnit.NANOSECONDS.toMillis(min);
    }

    public long maxMS() {
        return TimeUnit.NANOSECONDS.toMillis(longest.get());
    }

    public long hits() {
        return hits.get();
    }

    public double averageMS() {
        return timeMS() / Math.max(1.0, hits());
    }

    public ITextComponent toText() {
        return new StringTextComponent(name().toLowerCase())
                .append(new StringTextComponent(String.format(": %.3fms", averageMS()))
                        .modifyStyle(style -> style.forceFormatting(TextFormatting.WHITE)))
                .modifyStyle(style -> style.applyFormatting(TextFormatting.YELLOW)
                        .setHoverEvent(createHoverStats(minMS(), maxMS())));
    }

    public static HoverEvent createHoverStats(long min, long max) {
        String message = String.format("Min: %sms, Max: %sms", min, max);
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new StringTextComponent(message).modifyStyle(s -> s.forceFormatting(TextFormatting.WHITE)));
    }

    public static void reset() {
        for (Profiler profiler : Profiler.values()) {
            profiler.time.set(0);
            profiler.hits.set(0);
            profiler.longest.set(0);
            profiler.shortest.set(Long.MAX_VALUE);
        }
    }

    public static void dump(File dir) {
        dump(dir.toPath());
    }

    public static void dump(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve("dump-" + System.currentTimeMillis() + ".txt"))) {
                writer.write("Section");
                writer.write("\t");
                writer.write("Time MS");
                writer.write("\t");
                writer.write("Count");
                writer.write("\t");
                writer.write("Min MS");
                writer.write("\t");
                writer.write("Max MS");
                writer.write("\t");
                writer.write("Average MS");
                writer.write("\n");

                double averageSum = 0.0;
                for (Profiler profiler : Profiler.values()) {
                    writer.write(profiler.name().toLowerCase());
                    writer.write("\t");
                    writer.write(String.valueOf(profiler.timeMS()));
                    writer.write("\t");
                    writer.write(String.valueOf(profiler.hits()));
                    writer.write("\t");
                    writer.write(String.valueOf(profiler.minMS()));
                    writer.write("\t");
                    writer.write(String.valueOf(profiler.maxMS()));
                    writer.write("\t");
                    double average = profiler.averageMS();
                    averageSum += average;
                    writer.write(String.valueOf(average));
                    writer.write("\n");
                }

                writer.write("Sum");
                writer.write("\t");
                writer.write("\t");
                writer.write("\t");
                writer.write("\t");
                writer.write("\t");
                writer.write("\t");
                writer.write("\t");
                writer.write("\t");
                writer.write("\t");
                writer.write(String.valueOf(averageSum));
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ProfilerSection implements Section {

        private long timestamp = 0L;

        @Override
        public Section punchIn() {
            timestamp = System.nanoTime();
            return this;
        }

        @Override
        public void close() {
            long duration = System.nanoTime() - timestamp;
            time.addAndGet(duration);
            hits.incrementAndGet();

            long max = longest.get();
            if (duration > max) {
                longest.compareAndSet(max, duration);
            }

            long min = shortest.get();
            if (duration < min) {
                shortest.compareAndSet(min, duration);
            }
        }
    }
}
