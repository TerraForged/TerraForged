package com.terraforged.mod.settings;

import com.google.gson.Gson;
import com.mojang.datafixers.OptionalDynamic;
import com.terraforged.mod.Log;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.IWorld;
import net.minecraft.world.level.LevelGeneratorOptions;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

public class SettingsHelper {

    public static final String SETTINGS_FILE_NAME = "terraforged-generator.json";
    private static boolean dedicated = false;

    public static void setDedicatedServer() {
        dedicated = true;
    }

    public static int getVersion(LevelProperties info) {
        if (info.getGeneratorOptions().getDynamic().getMapValues().map(Map::isEmpty).orElse(true)) {
            // if options have not been set then the world has been created
            // during the current runtime .: is not legacy
//            return TerraWorld.VERSION; todo
            return 0;
        }

        OptionalDynamic<?> version = info.getGeneratorOptions().getDynamic().get("version");
        if (!version.get().isPresent()) {
            // version tag is absent in legacy worlds .: is legacy
            return 0;
        }

        return version.get().get().get("value").asInt(0); // blursed dfu
    }

    public static TerraSettings getSettings(IWorld world) {
        if (dedicated) {
            try (Reader reader = new BufferedReader(new FileReader(new File("config", SETTINGS_FILE_NAME)))) {
                Log.info("Loading generator settings from json");
                return new Gson().fromJson(reader, TerraSettings.class);
            } catch (Throwable ignored) {
                return getSettings(world.getLevelProperties());
            }
        }
        return getSettings(world.getLevelProperties());
    }

    public static TerraSettings getSettings(LevelProperties info) {
        TerraSettings settings = new TerraSettings();
        if (!info.getGeneratorOptions().getDynamic().getMapValues().map(Map::isEmpty).orElse(true)) {
            NBTHelper.deserialize((CompoundTag) info.getGeneratorOptions().getDynamic().convert(NbtOps.INSTANCE).getValue(), settings);
        }
        return settings;
    }

    public static void syncSettings(LevelProperties info, TerraSettings settings, int version) {
        settings.version = version;
        settings.generator.seed = info.getSeed();
        CompoundTag options = NBTHelper.serialize(settings);
        // todo find generator type and chunk generator factory
        //info.setGeneratorOptions(new LevelGeneratorOptions());
    }
}
