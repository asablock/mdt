package io.github.asablock.mdt.toggle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.asablock.mdt.toggle.enums.ChatMaxLengthBehavior;

import static io.github.asablock.mdt.toggle.ToggleFactory.*;

public final class Toggles {
    public static final BiMap<String, Toggle<?>> TOGGLES = HashBiMap.create();

    public static final Toggle<ChatMaxLengthBehavior> chatMaxLengthBehavior = reg(ofEnum("chatMaxLengthBehavior", ChatMaxLengthBehavior.RESTRICTED_AFTER_SENDING, ChatMaxLengthBehavior.class));
    public static final Toggle<Boolean> disableBlindness = reg(ofBool("disableBlindness", true));
    public static final Toggle<Boolean> disableDarkness = reg(ofBool("disableDarkness", true));
    public static final Toggle<Boolean> disableRespawnWait = reg(ofBool("disableRespawnWait", true));
    public static final Toggle<Boolean> chatOnDeath = reg(ofBool("chatOnDeath", true));

    public static <E> Toggle<E> reg(Toggle<E> toggle) {
        TOGGLES.put(toggle.name, toggle);
        return toggle;
    }

    public static int resetAll() {
        int modifications = 0;
        for (Toggle<?> value : Toggles.TOGGLES.values()) {
            if (value.reset()) modifications++;
        }
        return modifications;
    }

    private Toggles() {
    }
}
