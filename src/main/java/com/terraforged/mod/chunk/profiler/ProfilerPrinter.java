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

package com.terraforged.mod.chunk.profiler;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

public class ProfilerPrinter {

    private static final int TAB_WIDTH = 4;
    private static final String[] HEADERS = {"Section", "Count", "Time MS", "Min MS", "Max MS", "Average MS"};
    private static final int[] WIDTHS = new int[HEADERS.length];

    static {
        int widest = tabWidth(HEADERS[0]);
        for (Profiler profiler : Profiler.values()) {
            widest = Math.max(widest, tabWidth(profiler.name()));
        }

        WIDTHS[0] = widest;

        for (int i = 1; i < WIDTHS.length; i++) {
            WIDTHS[i] = tabWidth(HEADERS[i]);
        }
    }

    public static void print(Writer writer) throws IOException {
        print(writer, "");
    }

    public static void print(Writer writer, String indent) throws IOException {
        // headers
        writer.write(indent);
        for (int i = 0; i < HEADERS.length; i++) {
            append(i, HEADERS[i], writer);
        }

        // table contents
        double averageSum = 0.0;
        for (Profiler profiler : Profiler.values()) {
            double average = profiler.averageMS();
            averageSum += average;

            writer.write("\n");
            writer.write(indent);
            append(0, profiler.name().toLowerCase(Locale.ROOT), writer);
            append(1, fmt(profiler.hits()), writer);
            append(2, fmt(profiler.timeMS()), writer);
            append(3, fmt(profiler.minMS()), writer);
            append(4, fmt(profiler.maxMS()), writer);
            append(5, fmt(average), writer);
        }

        // footer
        writer.write("\n");
        writer.write(indent);
        append(0, "Sum", writer);
        for (int i = 1; i < WIDTHS.length; i++) {
            append(i, "", writer);
        }
        append(5, fmt(averageSum), writer);
    }

    private static void append(int col, String text, Writer writer) throws IOException {
        writer.write(text);

        if (col == WIDTHS.length - 1) {
            return;
        }

        int padding = WIDTHS[col] - text.length();
        int tabs = padding / TAB_WIDTH;
        if (tabs * TAB_WIDTH == padding) {
            tabs--;
        }

        for (int i = 0; i <= tabs; i++) {
            writer.write("\t");
        }
    }

    private static String fmt(long value) {
        return Long.toString(value);
    }

    private static String fmt(double value) {
        return String.format("%.2f", value);
    }

    private static int tabWidth(String input) {
        int tabs = 1 + input.length() / TAB_WIDTH;
        return tabs * TAB_WIDTH;
    }
}
