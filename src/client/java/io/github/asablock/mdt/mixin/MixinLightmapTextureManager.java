package io.github.asablock.mdt.mixin;

import io.github.asablock.mdt.Toggles;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public abstract class MixinLightmapTextureManager {
    @Shadow protected abstract float getDarknessFactor(float delta);

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/LightmapTextureManager;getDarknessFactor(F)F"))
    private float disableDarkness(LightmapTextureManager instance, float delta) {
        return Toggles.disableDarkness.enabled ? 0.0F : getDarknessFactor(delta);
    }
}
