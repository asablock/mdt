/*
 * This file is part of mdt. mdt is a client-side mod for Minecraft.
 * Copyright (C) 2025  asablock
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.asablock.mdt.toggle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.asablock.mdt.PlayerAlert;
import io.github.asablock.mdt.util.IOUtil;
import io.github.asablock.mdt.Mdt;
import io.github.asablock.mdt.toggle.enums.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.PrintStream;

import static io.github.asablock.mdt.toggle.ToggleFactory.*;

public final class Toggles {
    public static final BiMap<String, Toggle<?>> TOGGLES = HashBiMap.create();
    public static final BiMap<String, ToggleNode> TOGGLE_NODES = HashBiMap.create();

    private static final PrintStream MIXED_SYSOUT = IOUtil.createMultipleDelegatedPrintStream(false, Mdt.LOGGER_OUT_PRINT_STREAM, IOUtil.getTextStream(t -> IOUtil.chatAppender().accept(Text.literal("[SYSOUT] ").formatted(Formatting.AQUA).append(t))));
    private static final PrintStream MIXED_SYSERR = IOUtil.createMultipleDelegatedPrintStream(false, Mdt.LOGGER_ERROR_PRINT_STREAM, IOUtil.getTextStream(t -> IOUtil.chatAppender().accept(Text.literal("[SYSERR] ").formatted(Formatting.RED).append(t))));

    public static final ToggleDirectory root = regn(ToggleDirectory.createRootNode());

    public static final Toggle<ChatMaxLengthBehavior> chatMaxLengthBehavior = reg(ofEnum(root, "chatMaxLengthBehavior", ChatMaxLengthBehavior.VANILLA, ChatMaxLengthBehavior.class));
    public static final Toggle<Boolean> disableBlindness = reg(ofBool(root, "disableBlindness", false));
    public static final Toggle<Boolean> disableDarkness = reg(ofBool(root, "disableDarkness", false));
    public static final Toggle<Boolean> disableRespawnWait = reg(ofBool(root, "disableRespawnWait", false));
    public static final Toggle<Boolean> chatOnDeath = reg(ofBool(root, "chatOnDeath", false));
    public static final Toggle<Boolean> disableNightVision = reg(ofBool(root, "disableNightVision", false));
    public static final Toggle<Boolean> showBarrier = reg(ofBool(root, "showBarrier", false));
    public static final Toggle<Integer> sendByPartsIntervalMillis = reg(ofInt(root, "sendByPartsIntervalMillis", 400, 0, Integer.MAX_VALUE));
    public static final Toggle<Integer> sendByPartsPartSize = reg(ofInt(root, "sendByPartsPartSize", 256, 1, Integer.MAX_VALUE));

    public static final ToggleDirectory sysio = regn(ofDir(root, "sysio"));

    public static final Toggle<Boolean> sysio_sendSysOutToChat = reg(ofBool(sysio, "sendSysOutToChat", false, (o, n) -> System.setOut(n ? MIXED_SYSOUT : Mdt.LOGGER_OUT_PRINT_STREAM)));
    public static final Toggle<Boolean> sysio_sendSysErrToChat = reg(ofBool(sysio, "sendSysErrToChat", false, (o, n) -> System.setOut(n ? MIXED_SYSERR : Mdt.LOGGER_ERROR_PRINT_STREAM)));

    public static final ToggleDirectory playerAlert = regn(ofDir(root, "playerAlert"));

    public static final Toggle<Boolean> playerAlert_enabled = reg(ofBool(playerAlert, "enabled", false, (o, n) -> {
        if (!o && n) {
            PlayerAlert.clear();
        }
    }));
    public static final Toggle<Integer> playerAlert_radius = reg(ofInt(playerAlert, "radius", 128, 0, Integer.MAX_VALUE, (old, r) -> PlayerAlert.squaredAlertRadius = r * r));
    public static final Toggle<Boolean> playerAlert_alertSelf = reg(ofBool(playerAlert, "alertSelf", false));
    public static final Toggle<Boolean> playerAlert_sound = reg(ofBool(playerAlert, "sound", false));

    public static <E> Toggle<E> reg(Toggle<E> toggle) {
        regn(toggle);
        TOGGLES.put(toggle.getName(), toggle);
        return toggle;
    }

    public static <N extends ToggleNode> N regn(N node) {
        TOGGLE_NODES.put(node.getName(), node);
        return node;
    }

    public static int resetAll() {
        return root.reset();
    }

    private Toggles() {
    }

    public static void init() {
        // for static initialization
    }
}
