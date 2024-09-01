package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.Toggles;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @ModifyConstant(method = "init", constant = @Constant(intValue = 256))
    private int disableChatMaxLength(int value) {
        return Toggles.disableChatMaxLength.enabled ? Integer.MAX_VALUE : value;
    }
}
