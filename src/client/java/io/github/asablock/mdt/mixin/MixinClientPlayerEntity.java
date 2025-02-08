package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.toggle.Toggles;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Inject(method = "isBlind", at = @At("RETURN"), cancellable = true)
    private void isBlind(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!Toggles.disableBlindness.get() && cir.getReturnValueZ());
    }
}
