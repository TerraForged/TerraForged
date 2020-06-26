package com.terraforged.mod.chunk.settings.preset;

import com.terraforged.mod.chunk.settings.TerraSettings;

import java.io.File;

public class Preset implements Comparable<Preset> {

    private final boolean changed;
    private final File file;
    private final String id;
    private final String name;
    private final TerraSettings settings;

    Preset(String name, File file, TerraSettings settings) {
        this.name = name;
        this.file = file;
        this.changed = false;
        this.settings = settings;
        this.id = name.toLowerCase();
    }

    public Preset(String name, TerraSettings settings) {
        this.name = name;
        this.changed = true;
        this.settings = settings;
        this.id = name.toLowerCase();
        this.file = new File(PresetManager.PRESETS_DIR, name + ".json");
    }

    public boolean changed() {
        return changed;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public TerraSettings getSettings() {
        return settings;
    }

    @Override
    public int compareTo(Preset o) {
        return id.compareTo(o.id);
    }
}
