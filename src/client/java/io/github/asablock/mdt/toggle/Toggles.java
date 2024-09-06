package io.github.asablock.mdt.toggle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.brigadier.arguments.BoolArgumentType;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public final class Toggles {
    public static final BiMap<String, Toggle<?>> TOGGLES = HashBiMap.create();

    public static final Toggle<Boolean> disableChatFieldMaxLength = ofBoolean("disableChatFieldMaxLength", true);
    public static final Toggle<Boolean> disableBlindness = ofBoolean("disableBlindness", true);
    public static final Toggle<Boolean> disableDarkness = ofBoolean("disableDarkness", true);
    public static final Toggle<Boolean> restrictMaxLengthForSentChat = ofBoolean("restrictMaxLengthForSentChat", true);
    public static final Toggle<Boolean> disableRespawnWait = ofBoolean("disableRespawnWait", true);
    public static final Toggle<Boolean> chatOnDeath = ofBoolean("chatOnDeath", true);

    public static Toggle<Boolean> ofBoolean(String name, boolean defaultValue) {
        Toggle<Boolean> toggle = new Toggle<>(name, defaultValue, b -> b instanceof Boolean, Toggle.doNothing(),
                Object::toString,
                executes -> argument("value", BoolArgumentType.bool()).executes(executes).build(),
                ctx -> BoolArgumentType.getBool(ctx, "value"));
        TOGGLES.put(name, toggle);
        return toggle;
    }

    private Toggles() {
    }
}
