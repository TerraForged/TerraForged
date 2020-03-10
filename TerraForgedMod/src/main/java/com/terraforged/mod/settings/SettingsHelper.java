package com.terraforged.mod.settings;

import com.google.gson.Gson;
import com.terraforged.mod.Log;
import com.terraforged.mod.TerraWorld;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.storage.WorldInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class SettingsHelper {

    public static final String SETTINGS_FILE_NAME = "terraforged-generator.json";
    private static boolean dedicated = false;

    public static void setDedicatedServer() {
        dedicated = true;
    }

    public static int getVersion(WorldInfo info) {
        if (info.getGeneratorOptions().isEmpty()) {
            // if options have not been set then the world has been created
            // during the current runtime .: is not legacy
            return TerraWorld.VERSION;
        }

        CompoundNBT version = info.getGeneratorOptions().getCompound("version");
        if (version.isEmpty()) {
            // version tag is absent in legacy worlds .: is legacy
            return 0;
        }

        return version.getInt("value");
    }

    public static TerraSettings getSettings(IWorld world) {
        if (dedicated) {
            try (Reader reader = new BufferedReader(new FileReader(new File("config", SETTINGS_FILE_NAME)))) {
                Log.info("Loading generator settings from json");
                return new Gson().fromJson(reader, TerraSettings.class);
            } catch (Throwable ignored) {
                return getSettings(world.getWorldInfo());
            }
        }
        return getSettings(world.getWorldInfo());
    }

    public static TerraSettings getSettings(WorldInfo info) {
        TerraSettings settings = new TerraSettings();
        if (!info.getGeneratorOptions().isEmpty()) {
            NBTHelper.deserialize(info.getGeneratorOptions(), settings);
        }
        return settings;
    }

    public static void syncSettings(WorldInfo info, TerraSettings settings, int version) {
        settings.version = version;
        settings.generator.seed = info.getSeed();
        CompoundNBT options = NBTHelper.serialize(settings);
        info.setGeneratorOptions(options);
    }
}
