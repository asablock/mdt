package io.github.asablock.mdt.mixin;

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
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DeathScreen.class)
public abstract class MixinDeathScreen extends Screen {
    protected MixinDeathScreen(Text message) {
        super(message);
    }

    @Shadow protected abstract void setButtonsActive(boolean active);

    @Shadow @Final private List<ButtonWidget> buttons;

    @Shadow protected abstract void quitLevel();

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/DeathScreen;setButtonsActive(Z)V"))
    private void disableRespawnWait(DeathScreen instance, boolean active) {
        if (!Toggles.disableRespawnWait.get()) setButtonsActive(active);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addChatButton(CallbackInfo ci) {
        if (Toggles.chatOnDeath.get()) {
            DeathScreen self = Mdt.cast(this);
            ScreenInvoker screenInvoker = (ScreenInvoker) self;
            buttons.add(screenInvoker.invokeAddDrawableChild(
                    ButtonWidget.builder(Text.translatable("mdt.deathScreen.openChat"),
                            button -> ((MinecraftClientInvoker) screenInvoker.getClient()).invokeOpenChatScreen(""))
                            .dimensions(self.width / 2 - 100, self.height / 4 + 120, 200, 20)
                            .build())
            );
        }
    }

    @Redirect(method = "onTitleScreenButtonClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ConfirmScreen;disableButtons(I)V"))
    private void disableConfirmScreenWait(ConfirmScreen instance, int ticks) {
        if (!Toggles.disableRespawnWait.get()) instance.disableButtons(ticks);
    }

    @Inject(method = "method_47939", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;requestRespawn()V"), cancellable = true)
    private void returnToDeathScreen(boolean confirmed, CallbackInfo ci) {
        if (Toggles.chatOnDeath.get()) {
            client.setScreen(this);
            ci.cancel();
        }
    }

    @ModifyConstant(method = "onTitleScreenButtonClicked", constant = @Constant(stringValue = "deathScreen.respawn"))
    private String modifyButtonName(String constant) {
        return Toggles.chatOnDeath.get() ? "gui.cancel" : constant;
    }
}
