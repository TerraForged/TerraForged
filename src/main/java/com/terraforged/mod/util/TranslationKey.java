package com.terraforged.mod.util;

import com.terraforged.core.util.NameUtil;
import net.minecraft.client.resources.I18n;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TranslationKey {

    private static final Map<String, TranslationKey> keys = new HashMap<>();

    private final String translationKey;
    private final String defaultValue;

    public TranslationKey(String key, String display) {
        this.translationKey = key;
        this.defaultValue = display;
        keys.put(translationKey, this);
    }

    public String getKey() {
        return translationKey;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String get() {
        if (I18n.hasKey(translationKey)) {
            return I18n.format(translationKey);
        }
        return defaultValue;
    }

    public String get(Object... args) {
        if (I18n.hasKey(translationKey)) {
            return I18n.format(translationKey, args);
        }
        return defaultValue;
    }

    public static void each(Consumer<TranslationKey> consumer) {
        keys.values().stream().sorted(Comparator.comparing(TranslationKey::getKey)).forEach(consumer);
    }

    public static TranslationKey gui(String text) {
        String key = NameUtil.toDisplayNameKey(text);
        String display = NameUtil.toDisplayName(text.substring(text.lastIndexOf('.') + 1));
        return new TranslationKey(key, display);
    }

    public static TranslationKey gui(String key, String display) {
        return new TranslationKey(NameUtil.toDisplayNameKey(key), display);
    }
}
