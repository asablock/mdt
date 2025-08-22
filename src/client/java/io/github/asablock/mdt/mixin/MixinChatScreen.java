package io.github.asablock.mdt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
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
        if (Toggles.chatMaxLengthBehavior.get().shallRestrictFieldMaxLength()) {
            chatField.setMaxLength(Integer.MAX_VALUE);
        }
    }

    @WrapOperation(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V"))
    private void sendChatMessageRestrict(ClientPlayNetworkHandler instance, String content, Operation<Void> original) {
        original.call(instance, Toggles.chatMaxLengthBehavior.get().shallRestrictAfterSending() ? (content.length() > 256 ? content.substring(0, 256) : content) : content);
    }
}
