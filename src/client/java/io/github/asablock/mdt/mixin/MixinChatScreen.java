package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.command.JavaShellCommand;
import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @ModifyConstant(method = "init", constant = @Constant(intValue = 256))
    private int disableChatMaxLength(int value) {
        return Toggles.chatMaxLengthBehavior.get().shallRestrictFieldMaxLength() ? value : Integer.MAX_VALUE;
    }

    @Redirect(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendChatMessage(Ljava/lang/String;)V"))
    private void sendChatMessageRestrict(ClientPlayNetworkHandler instance, String content) {
        instance.sendChatMessage(Toggles.chatMaxLengthBehavior.get().shallRestrictAfterSending() ? (content.length() > 256 ? content.substring(0, 256) : content) : content);
    }

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void javaShellImmersiveMode(String chatText, boolean addToHistory, CallbackInfo ci) {
        if (Toggles.javaShell_immersiveMode.get()) {
            JavaShellCommand.appendLine(chatText);
            ci.cancel();
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatInputSuggestor;render(Lnet/minecraft/client/gui/DrawContext;II)V"))
    private void disableInputSuggestion(ChatInputSuggestor instance, DrawContext context, int mouseX, int mouseY) {
        if (!Toggles.javaShell_immersiveMode.get()) {
            instance.render(context, mouseX, mouseY);
        }
    }
}
