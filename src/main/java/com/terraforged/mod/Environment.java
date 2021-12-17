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

package com.terraforged.mod;

public interface Environment {
    boolean DEV_ENV = hasFlag("dev");
    boolean PROFILING = DEV_ENV || hasFlag("profiling");
    boolean UNLIMITED = DEV_ENV || hasFlag("unlimited");
    boolean DEBUGGING = DEV_ENV || hasFlag("debugging");
    boolean DATA_GEN = DEV_ENV || hasFlag("datagen");
    int CORES = Runtime.getRuntime().availableProcessors();

    static boolean hasFlag(String flag) {
        return System.getProperty(flag) != null;
    }

    static void log() {
        TerraForged.LOG.info("Environment:");
        TerraForged.LOG.info("- Dev:       {}", DEV_ENV);
        TerraForged.LOG.info("- Profiling: {}", PROFILING);
        TerraForged.LOG.info("- Unlimited: {}", UNLIMITED);
        TerraForged.LOG.info("- Debugging: {}", DEBUGGING);
        TerraForged.LOG.info("- Data Gen:  {}", DATA_GEN);
        TerraForged.LOG.info("- Cores:     {}", CORES);
    }
}
