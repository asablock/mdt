package io.github.asablock.mdt;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public final class Toggles {
    public static final BiMap<String, Toggle> TOGGLES = HashBiMap.create();

    public static final Toggle disableChatFieldMaxLength = of("disableChatFieldMaxLength", true);
    public static final Toggle disableBlindness = of("disableBlindness", true);
    public static final Toggle disableDarkness = of("disableDarkness", true);
    public static final Toggle restrictMaxLengthForSentChat = of("restrictMaxLengthForSentChat", true);

    private static Toggle of(String name, boolean defaultValue) {
        Toggle toggle = new Toggle(name, defaultValue);
        TOGGLES.put(name, toggle);
        return toggle;
    }

    public static boolean isEnabled(String name) {
        Toggle toggle = TOGGLES.get(name);
        return toggle != null && toggle.enabled;
    }

    private Toggles() {
    }
}
