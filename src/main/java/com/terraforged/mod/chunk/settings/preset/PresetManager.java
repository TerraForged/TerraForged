package com.terraforged.mod.chunk.settings.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.settings.SettingsHelper;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class PresetManager implements Iterable<Preset> {

    public static final File PRESETS_DIR = new File(SettingsHelper.SETTINGS_DIR, "presets");

    private final List<Preset> presets;
    private final List<Preset> deleted = new ArrayList<>();

    private PresetManager(List<Preset> presets) {
        this.presets = presets;
    }

    public Optional<Preset> get(String name) {
        for (Preset preset : presets) {
            if (preset.getName().equalsIgnoreCase(name)) {
                return Optional.of(preset);
            }
        }
        return Optional.empty();
    }

    public void add(Preset preset) {
        // replace a preset by the same name
        for (int i = 0; i < presets.size(); i++) {
            if (presets.get(i).getId().equalsIgnoreCase(preset.getId())) {
                presets.set(i, preset);
                return;
            }
        }

        // otherwise add to end of list
        presets.add(preset);
        Collections.sort(presets);
    }

    public void remove(String name) {
        get(name).ifPresent(preset -> {
            presets.remove(preset);
            deleted.add(preset);
        });
    }

    public void saveAll() {
        if (!ensureDir()) {
            Log.err("Unable to save presets to disk");
            return;
        }

        for (Preset preset : deleted) {
            if (preset.getFile().exists() && preset.getFile().delete()) {
                Log.debug("Deleted preset: {}", preset.getName());
            } else {
                Log.debug("Unable to delete file: {}", preset.getFile());
            }
        }

        Log.info("Saving presets");
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        for (Preset preset : presets) {
            if (!preset.changed()) {
                continue;
            }

            Log.debug("Saving preset: {}", preset.getName());
            CompoundNBT nbt = NBTHelper.serializeCompact(preset.getSettings());
            JsonElement json = NBTHelper.toJson(nbt);
            try (Writer writer = new BufferedWriter(new FileWriter(preset.getFile()))) {
                gson.toJson(json, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Iterator<Preset> iterator() {
        return presets.iterator();
    }

    private static boolean ensureDir() {
        if (!PRESETS_DIR.exists() && !PRESETS_DIR.mkdirs()) {
            Log.err("Unable to create presets directory: {}", PRESETS_DIR);
            return false;
        }
        return true;
    }

    public static PresetManager load() {
        if (!ensureDir()) {
            return new PresetManager(new ArrayList<>());
        }

        File[] files = PRESETS_DIR.listFiles();
        if (files == null) {
            return new PresetManager(new ArrayList<>());
        }

        List<Preset> presets = new ArrayList<>(files.length);
        for (File file : files) {
            if (!file.getName().endsWith(".json")) {
                continue;
            }

            String name = file.getName().substring(0, file.getName().length() - 5);
            TerraSettings settings = SettingsHelper.loadSettings(file);
            presets.add(new Preset(name, file, settings));
        }

        return new PresetManager(presets);
    }
}
