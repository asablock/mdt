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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.ClientDelayedTask;
import io.github.asablock.mdt.toggle.Toggles;
import io.github.asablock.mdt.toggle.enums.ChatMaxLengthBehavior;
import io.github.asablock.mdt.util.Util;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {
    @Shadow protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At("TAIL"), order = 1100)
    private void disableChatMaxLength(CallbackInfo ci) {
        if (!Toggles.chatMaxLengthBehavior.get().shallRestrictFieldMaxLength()) {
            chatField.setMaxLength(Integer.MAX_VALUE);
        }
    }

    @WrapOperation(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;normalize(Ljava/lang/String;)Ljava/lang/String;"))
    private String noNormalize(ChatScreen instance, String chatText, Operation<String> original) {
        return Toggles.chatMaxLengthBehavior.get().shallRestrictAfterSending() ? original.call(instance, chatText) : chatText;
    }

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void sendByParts(String chatText, boolean addToHistory, CallbackInfo ci) {
        if (Toggles.chatMaxLengthBehavior.get() == ChatMaxLengthBehavior.SEND_BY_PARTS && chatText.length() > Toggles.sendByPartsPartSize.get()) {
            long now = System.currentTimeMillis();
            int partSize = Toggles.sendByPartsPartSize.get();
            int interval = Toggles.sendByPartsIntervalMillis.get();
            int chunks = (chatText.length() - 1) / partSize + 1;
            for (int i = 0; i < chunks - 1; i++) {
                final int index = i;
                ClientDelayedTask task = new ClientDelayedTask(now + (long) interval * i, client -> {
                    String chunk = chatText.substring(partSize * index, partSize * index + partSize);
                    Util.sendChat(chunk, addToHistory);
                });
                ClientDelayedTask.schedule(task);
            }
            ClientDelayedTask task = new ClientDelayedTask(now + (long) interval * (chunks - 1), client -> {
                String chunk = chatText.substring(partSize * (chunks - 1));
                Util.sendChat(chunk, addToHistory);
            });
            ClientDelayedTask.schedule(task);
            ci.cancel();
        }
    }
}
