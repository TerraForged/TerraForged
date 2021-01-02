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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.terraforged.mod.Log;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.chunk.settings.SettingsHelper;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.util.DataUtils;
import com.terraforged.mod.util.function.IOFunction;
import com.terraforged.mod.util.function.IOSupplier;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.ModList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PresetManager implements Iterable<Preset> {

    public static final File PRESETS_DIR = new File(SettingsHelper.SETTINGS_DIR, "presets");

    private final List<Preset> presets;
    private final List<Preset> deleted = new ArrayList<>();

    private PresetManager(List<Preset> presets) {
        this.presets = presets;
        Collections.sort(presets);
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
            Preset current = presets.get(i);
            if (!current.internal() && current.getId().equalsIgnoreCase(preset.getId())) {
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
            if (!preset.changed() || preset.internal()) {
                continue;
            }

            Log.debug("Saving preset: {}", preset.getName());
            JsonElement json = DataUtils.toJson(preset.getSettings());
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

        loadInternalPresets(presets);

        return new PresetManager(presets);
    }

    private static String getDescription(JsonElement json) {
        if (json.isJsonObject()) {
            JsonElement description = json.getAsJsonObject().get("#description");
            if (description != null) {
                return description.getAsString();
            }
        }
        return "";
    }

    private static void setDescription(JsonElement json, String description) {
        if (json.isJsonObject()) {
            json.getAsJsonObject().addProperty("#description", description);
        }
    }

    private static void loadInternalPresets(List<Preset> presets) {
        Path path = ModList.get().getModFileById(TerraForgedMod.MODID).getFile().getFilePath();
        if (Files.isDirectory(path)) {
            loadAll(() -> Files.walk(path), p -> toString(path.relativize(p)), Files::newInputStream, presets::add);
        } else {
            try (ZipFile zipFile = new ZipFile(path.toFile())) {
                loadAll(zipFile::stream, PresetManager::toString, zipFile::getInputStream, presets::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static <E> void loadAll(IOSupplier<Stream<E>> supplier, Function<E, String> name, IOFunction<E, InputStream> data, Consumer<Preset> consumer) {
        try {
            supplier.get().filter(e -> filter(name.apply(e))).forEach(e -> {
                try (InputStream in = data.apply(e)) {
                    loadPreset(name.apply(e), in, consumer);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String toString(Path path) {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < path.getNameCount(); i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(path.getName(i));
        }
        return sb.toString();
    }

    private static String toString(ZipEntry entry) {
        return entry.getName();
    }

    private static boolean filter(String path) {
        return path.startsWith("presets/") && path.endsWith(".json");
    }

    private static String parseName(String path) {
        int start = path.lastIndexOf('/') + 1;
        int end = Math.max(start + 1, path.lastIndexOf('.'));
        return path.substring(start, end);
    }

    private static void loadPreset(String path, InputStream in, Consumer<Preset> presets) {
        try (Reader reader = new BufferedReader(new InputStreamReader(in))) {
            TerraSettings settings = new TerraSettings();
            JsonElement data = new JsonParser().parse(reader);
            CompoundNBT nbt = DataUtils.fromJson(data);
            if (DataUtils.fromNBT(nbt, settings)) {
                String name = parseName(path);
                String description = getDescription(data);
                Preset preset = new Preset(name, description, settings, true);
                presets.accept(preset);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
