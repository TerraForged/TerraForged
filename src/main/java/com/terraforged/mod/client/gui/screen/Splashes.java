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

package com.terraforged.mod.client.gui.screen;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class Splashes {

    private static final String[] SPLASHES = {
            "Remember to back up your worlds!",
            "Try the different map render-modes!",
            "Provide your logs when reporting issues!",
            "Turn off the 'Strata Decorator' to get plain stone everywhere!",
            "Turn on 'Vanilla Lakes' if you need more water sources!",
            "Rivers flatten terrain, don't add too many if you like mountains!",
            "Presets are currently unavailable (they'll be coming back soon)",
            "Pushing settings to the extreme may cause weird things to happen!",
            "You may be able to configure structure placement via datapacks",
            "Erosion can really slow performance. Don't set it too high",
    };

    static {
        for (int i = 0; i < SPLASHES.length; i++) {
            SPLASHES[i] = " - " + SPLASHES[i].toUpperCase(Locale.ENGLISH);
        }
    }

    private static final AtomicInteger LAST = new AtomicInteger(-1);

    public static String next() {
        return SPLASHES[nextIndex()];
    }

    private static int nextIndex() {
        int last = LAST.get();
        int next = ThreadLocalRandom.current().nextInt(SPLASHES.length);
        if (next == last) {
            next++;
            if (next >= SPLASHES.length) {
                next = 0;
            }
        }
        LAST.set(next);
        return next;
    }
}
