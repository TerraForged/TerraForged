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

package com.terraforged.mod.chunk.settings.preset;

import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.chunk.settings.TerraSettings;

import java.io.File;

public class Preset implements Comparable<Preset> {

    public static final String DEFAULT_NAME = "Default";
    public static final String DEFAULT_KEY = "default_preset";

    private final boolean changed;
    private final boolean internal;
    private final File file;
    private final String id;
    private final String name;
    private final String description;
    private final TerraSettings settings;

    Preset(String name, File file, TerraSettings settings) {
        this.name = name;
        this.file = file;
        this.changed = false;
        this.internal = false;
        this.settings = settings;
        this.id = name.toLowerCase();
        this.description = "";
    }

    Preset(String name, String description, TerraSettings settings, boolean internal) {
        this.name = name;
        this.file = null;
        this.changed = false;
        this.settings = settings;
        this.id = name.toLowerCase();
        this.description = description;
        this.internal = internal;
    }

    public Preset(String name, TerraSettings settings) {
        this(name, "", settings);
    }

    public Preset(String name, String description, TerraSettings settings) {
        this.name = name;
        this.changed = true;
        this.internal = false;
        this.settings = settings;
        this.id = name.toLowerCase();
        this.description = description;
        this.file = new File(TerraForgedMod.PRESETS_DIR, name + ".json");
    }

    public boolean changed() {
        return changed;
    }

    public boolean internal() {
        return internal;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public File getFile() {
        return file;
    }

    public TerraSettings getSettings() {
        return settings;
    }

    public TerraSettings getSettings(long seed) {
        settings.world.seed = seed;
        return settings;
    }

    @Override
    public int compareTo(Preset o) {
        if (this.internal() && !o.internal()) {
            return 1;
        }
        if (o.internal() && !this.internal()) {
            return -1;
        }
        return id.compareTo(o.id);
    }
}
