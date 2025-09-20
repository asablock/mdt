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

package io.github.asablock.mdt;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;

import java.util.HashSet;
import java.util.Set;

public final class EnhancedKeyBindingHelper {
    private EnhancedKeyBindingHelper() {
    }

    private static final Set<KeyBinding> SCREEN_KEY_BINDINGS = new HashSet<>();

    public static KeyBinding addScreenKeyBinding(KeyBinding keyBinding) {
        SCREEN_KEY_BINDINGS.add(keyBinding);
        return keyBinding;
    }

    public static boolean isScreenKeyBinding(KeyBinding keyBinding) {
        return SCREEN_KEY_BINDINGS.contains(keyBinding);
    }

    public static boolean removeScreenKeyBinding(KeyBinding keyBinding) {
        return SCREEN_KEY_BINDINGS.remove(keyBinding);
    }

    public static KeyBinding registerScreenKeyBinding(KeyBinding keyBinding) {
        return addScreenKeyBinding(KeyBindingHelper.registerKeyBinding(keyBinding));
    }
}
