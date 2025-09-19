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

package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.EnhancedKeyBindingHelper;
import net.minecraft.client.Keyboard;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Inject(method = "onKey", at = @At(value = "RETURN", ordinal = 4))
    private static void screenKeyReleased(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        InputUtil.Key key2 = InputUtil.fromKeyCode(key, scancode);
        KeyBinding keyBinding = KeyBindingAccessor.getKeyToBindings().get(key2);
        if (keyBinding != null && EnhancedKeyBindingHelper.isScreenKeyBinding(keyBinding)) {
            keyBinding.setPressed(false);
        }
    }

    @Inject(method = "onKey", at = @At(value = "RETURN", ordinal = 3))
    private static void screenKeyPressed(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        InputUtil.Key key2 = InputUtil.fromKeyCode(key, scancode);
        KeyBinding keyBinding = KeyBindingAccessor.getKeyToBindings().get(key2);
        if (keyBinding != null && EnhancedKeyBindingHelper.isScreenKeyBinding(keyBinding)) {
            keyBinding.setPressed(true);
            KeyBindingAccessor kbda = (KeyBindingAccessor) keyBinding;
            kbda.setTimesPressed(kbda.getTimesPressed() + 1);
        }
    }
}
