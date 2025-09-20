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

import io.github.asablock.mdt.util.Util;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.Component;
import org.lwjgl.glfw.GLFW;

public final class MdtKeyBindings {
    private MdtKeyBindings() {
    }

    public static final KeyBinding INTERACT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mdt.interact", GLFW.GLFW_KEY_UNKNOWN, "key.category.mdt"
    ));

    public static final KeyBinding VIEW_DATA_COMPONENTS = EnhancedKeyBindingHelper.registerScreenKeyBinding(new KeyBinding(
            "key.mdt.view_data_components", GLFW.GLFW_KEY_UNKNOWN, "key.category.mdt"
    ));

    public static final KeyBinding PRINT_DATA_COMPONENTS_IN_CHAT = EnhancedKeyBindingHelper.registerScreenKeyBinding(new KeyBinding(
            "key.mdt.print_data_components_in_chat", GLFW.GLFW_KEY_UNKNOWN, "key.category.mdt"
    ));

    static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen == null) {
                while (INTERACT.wasPressed()) {
                    Util.applyHitResult(client.crosshairTarget);
                }
            }
            while (PRINT_DATA_COMPONENTS_IN_CHAT.wasPressed()) {
                if (Mdt.viewDataComponentsStack != null) {
                    for (Component<?> component : Mdt.viewDataComponentsStack.getComponents()) {
                        Util.sendMessage(Util.toText(component));
                    }
                    Mdt.viewDataComponentsStack = null;
                }
            }
        });
    }
}
