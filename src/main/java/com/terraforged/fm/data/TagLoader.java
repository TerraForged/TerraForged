package com.terraforged.fm.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Set;

public class TagLoader {

    static <T extends IForgeRegistryEntry<T>> void loadTag(JsonObject object, ITag.INamedTag<T> tag, IForgeRegistry<T> registry, Set<T> set) {
        if (object.has("replace") && object.get("replace").getAsBoolean()) {
            set.clear();
        }

        if (object.has("values")) {
            for (JsonElement element : object.get("values").getAsJsonArray()) {
                ResourceLocation resourceName = ResourceLocation.tryCreate(element.getAsString());
                if (resourceName == null) {
                    continue;
                }

                T value = registry.getValue(resourceName);
                if (value != null) {
                    set.add(value);
                }
            }
        }
    }
}
