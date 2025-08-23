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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.Mdt;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DeathScreen.class)
public abstract class MixinDeathScreen extends Screen {
    protected MixinDeathScreen(Text message) {
        super(message);
    }

    @Shadow protected abstract void setButtonsActive(boolean active);

    @Shadow @Final private List<ButtonWidget> buttons;


    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/DeathScreen;setButtonsActive(Z)V"))
    private void disableRespawnWait(DeathScreen instance, boolean active, Operation<Void> original) {
        if (!Toggles.disableRespawnWait.get()) original.call(instance, active);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addChatButton(CallbackInfo ci) {
        if (Toggles.chatOnDeath.get()) {
            DeathScreen self = Mdt.cast(this);
            final ScreenInvoker screenInvoker = (ScreenInvoker) self;
            buttons.add(screenInvoker.invokeAddDrawableChild(
                    ButtonWidget.builder(Text.translatable("mdt.deathScreen.openChat"),
                            button -> ((MinecraftClientInvoker) screenInvoker.getClient()).invokeOpenChatScreen(""))
                            .dimensions(self.width / 2 - 100, self.height / 4 + 120, 200, 20)
                            .build())
            );
        }
    }

    @WrapOperation(method = "onTitleScreenButtonClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ConfirmScreen;disableButtons(I)V"))
    private void disableConfirmScreenWait(ConfirmScreen instance, int ticks, Operation<Void> original) {
        if (!Toggles.disableRespawnWait.get()) original.call(instance, ticks);
    }

    @Inject(method = "method_47939", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;requestRespawn()V"), cancellable = true)
    private void returnToDeathScreen(boolean confirmed, CallbackInfo ci) {
        if (Toggles.chatOnDeath.get()) {
            client.setScreen(this);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "onTitleScreenButtonClicked", at = @At(value = "CONSTANT", args = "stringValue=deathScreen.respawn"))
    private String modifyButtonName(String original) {
        return Toggles.chatOnDeath.get() ? "gui.cancel" : original;
    }
}
