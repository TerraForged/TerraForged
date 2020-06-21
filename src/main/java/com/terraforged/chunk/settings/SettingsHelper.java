package com.terraforged.chunk.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.terraforged.Log;
import com.terraforged.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;

public class SettingsHelper {

    public static final String SETTINGS_FILE_NAME = "terraforged-generator.json";
    public static final File SETTINGS_DIR = new File("config", "terraforged");
    public static final File SETTINGS_FILE= new File(SETTINGS_DIR, SETTINGS_FILE_NAME);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void clearDefaults() {
        if (SETTINGS_FILE.exists() && SETTINGS_FILE.delete()) {
            Log.info("Deleted generator defaults");
        }
    }

    public static void exportDefaults(TerraSettings settings) {
        CompoundNBT tag = NBTHelper.serializeCompact(settings);
        JsonElement json = NBTHelper.toJson(tag);
        try (Writer writer = new BufferedWriter(new FileWriter(SETTINGS_FILE))) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CompoundNBT applyDefaults(CompoundNBT options, TerraSettings dest) {
        if (options.isEmpty()) {
            try (Reader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
                JsonElement json = new JsonParser().parse(reader);
                options = NBTHelper.fromJson(json);
            } catch (IOException ignored) {

            }
        }
        NBTHelper.deserialize(options, dest);
        return options;
    }

    public static TerraSettings getSettings(WorldInfo info) {
        TerraSettings settings = new TerraSettings();
        if (info.getGeneratorOptions().isEmpty()) {
            if (SETTINGS_FILE.exists()) {
                try (Reader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
                    Log.info("Loading generator settings from json");
                    JsonElement json = new JsonParser().parse(reader);
                    CompoundNBT root = NBTHelper.fromJson(json);
                    NBTHelper.deserialize(root, settings);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else {
            Log.info("Loading generator settings from level.dat");
            NBTHelper.deserialize(info.getGeneratorOptions(), settings);
        }
        return settings;
    }

    public static void init() {
        if (SETTINGS_FILE.exists()) {
            return;
        }

        File src = new File("config", SETTINGS_FILE_NAME);
        if (src.exists()) {
            if (SETTINGS_DIR.exists() || SETTINGS_DIR.mkdirs()) {
                try {
                    Files.copy(src.toPath(), SETTINGS_FILE.toPath());
                    if (src.delete()) {
                        Log.info("Moved settings file to new location: {}", SETTINGS_FILE.getAbsoluteFile());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> exportDefaults(new TerraSettings()));
        }
    }
}
