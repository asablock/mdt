package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.Toggles;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @ModifyConstant(method = "init", constant = @Constant(intValue = 256))
    private int disableChatMaxLength(int value) {
        return Toggles.disableChatFieldMaxLength.enabled ? Integer.MAX_VALUE : value;
    }

    @Redirect(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V"))
    private void sendChatMessageRestrict(ClientPlayNetworkHandler instance, String content) {
        instance.sendChatMessage(Toggles.restrictMaxLengthForSentChat.enabled ? content.substring(0, 256) : content);
    }
}
