package io.github.asablock.mdt.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {
    @ModifyExpressionValue(method = "init", at = @At(value = "CONSTANT", args = "intValue=256"))
    private int disableChatMaxLength(int original) {
        return Toggles.chatMaxLengthBehavior.get().shallRestrictFieldMaxLength() ? original : Integer.MAX_VALUE;
    }

    @WrapOperation(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V"))
    private void sendChatMessageRestrict(ClientPlayNetworkHandler instance, String content, Operation<Void> original) {
        original.call(instance, Toggles.chatMaxLengthBehavior.get().shallRestrictAfterSending() ? (content.length() > 256 ? content.substring(0, 256) : content) : content);
    }
}
