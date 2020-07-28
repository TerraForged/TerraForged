package com.terraforged.mod.chunk.settings.preset;

import com.terraforged.mod.chunk.settings.TerraSettings;

import java.io.File;

public class Preset implements Comparable<Preset> {

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
        this.file = new File(PresetManager.PRESETS_DIR, name + ".json");
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
