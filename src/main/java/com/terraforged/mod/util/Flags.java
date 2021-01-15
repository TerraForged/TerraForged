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

public class Flags {

    public static final int OPTION_1 = 1;
    public static final int OPTION_2 = 1 << 1;
    public static final int OPTION_3 = 1 << 2;
    public static final int OPTION_4 = 1 << 3;
    public static final int OPTION_5 = 1 << 4;

    public static boolean has(int value, int flag) {
        return (value & flag) == flag;
    }

    public static int get(boolean opt1, boolean opt2) {
        int flags = opt1 ? OPTION_1 : 0;
        flags = opt2 ? flags | OPTION_2 : flags;
        return flags;
    }

    public static int get(boolean opt1, boolean opt2, boolean opt3) {
        int flags = get(opt1, opt2);
        return opt3 ? flags | OPTION_3 : flags;
    }

    public static int get(boolean opt1, boolean opt2, boolean opt3, boolean opt4) {
        int flags = get(opt1, opt2, opt3);
        return opt4 ? flags | OPTION_4 : flags;
    }

    public static int get(boolean opt1, boolean opt2, boolean opt3, boolean opt4, boolean opt5) {
        int flags = get(opt1, opt2, opt3, opt4);
        return opt5 ? flags | OPTION_5 : flags;
    }
}
