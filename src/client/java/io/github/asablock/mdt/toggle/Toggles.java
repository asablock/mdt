package io.github.asablock.mdt.toggle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.asablock.mdt.toggle.enums.ChatMaxLengthBehavior;

import static io.github.asablock.mdt.toggle.ToggleFactory.*;

public final class Toggles {
    public static final BiMap<String, Toggle<?>> TOGGLES = HashBiMap.create();
    public static final BiMap<String, ToggleNode> TOGGLE_NODES = HashBiMap.create();

    public static final ToggleDirectory root = regn(ToggleDirectory.createRootNode());

    public static final Toggle<ChatMaxLengthBehavior> chatMaxLengthBehavior = reg(ofEnum(root, "chatMaxLengthBehavior", ChatMaxLengthBehavior.RESTRICTED_AFTER_SENDING, ChatMaxLengthBehavior.class));
    public static final Toggle<Boolean> disableBlindness = reg(ofBool(root, "disableBlindness", true));
    public static final Toggle<Boolean> disableDarkness = reg(ofBool(root, "disableDarkness", true));
    public static final Toggle<Boolean> disableRespawnWait = reg(ofBool(root, "disableRespawnWait", true));
    public static final Toggle<Boolean> chatOnDeath = reg(ofBool(root, "chatOnDeath", true));

    public static <E> Toggle<E> reg(Toggle<E> toggle) {
        regn(toggle);
        TOGGLES.put(toggle.getName(), toggle);
        return toggle;
    }

    public static <E extends ToggleNode> E regn(E node) {
        TOGGLE_NODES.put(node.getName(), node);
        return node;
    }

    public static int resetAll() {
        return root.reset();
    }

    private Toggles() {
    }

    public static void init() {
    }
}
